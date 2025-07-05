const { Server } = require("socket.io");
const jwt = require("jsonwebtoken");
const { prisma } = require("../config/database");

class SocketService {
  constructor() {
    this.io = null;
    this.connectedUsers = new Map();
  }

  initialize(server) {
    this.io = new Server(server, {
      cors: {
        origin: process.env.SOCKET_CORS_ORIGIN || "http://localhost:3000",
        methods: ["GET", "POST"],
      },
    });

    this.setupMiddleware();
    this.setupEventHandlers();

    console.log("✅ Socket.io server initialized");
  }

  setupMiddleware() {
    // Authentication middleware
    this.io.use(async (socket, next) => {
      try {
        const token = socket.handshake.auth.token;

        if (!token) {
          return next(new Error("Authentication error"));
        }

        const decoded = jwt.verify(token, process.env.JWT_SECRET);
        const user = await prisma.user.findUnique({
          where: { id: decoded.userId },
          select: { id: true, email: true, username: true, role: true },
        });

        if (!user) {
          return next(new Error("User not found"));
        }

        socket.user = user;
        next();
      } catch (error) {
        next(new Error("Authentication error"));
      }
    });
  }

  setupEventHandlers() {
    this.io.on("connection", (socket) => {
      console.log(
        `User connected: ${socket.user.username} (${socket.user.id})`
      );

      // Update user online status
      this.updateUserOnlineStatus(socket.user.id, true);

      // Store connected user
      this.connectedUsers.set(socket.user.id, {
        socketId: socket.id,
        user: socket.user,
        connectedAt: new Date(),
      });

      // Join user to their personal room
      socket.join(`user:${socket.user.id}`);

      // Join admin to admin room if admin
      if (["ADMIN", "MODERATOR", "SUPPORT"].includes(socket.user.role)) {
        socket.join("admin");
      }

      // Notify others about user online status
      socket.broadcast.emit("user-online", {
        userId: socket.user.id,
        username: socket.user.username,
      });

      // Handle chat room joining
      socket.on("join-chat-room", (roomId) => {
        socket.join(`chat:${roomId}`);
        console.log(`User ${socket.user.username} joined chat room: ${roomId}`);
      });

      // Handle chat room leaving
      socket.on("leave-chat-room", (roomId) => {
        socket.leave(`chat:${roomId}`);
        console.log(`User ${socket.user.username} left chat room: ${roomId}`);
      });

      // Handle chat messages
      socket.on("chat-message", async (data) => {
        try {
          const { roomId, content, type = "TEXT" } = data;

          // Save message to database
          const message = await prisma.message.create({
            data: {
              roomId,
              senderId: socket.user.id,
              content,
              type,
            },
            include: {
              sender: {
                select: {
                  id: true,
                  username: true,
                  avatar: true,
                },
              },
            },
          });

          // Update room's updatedAt
          await prisma.chatRoom.update({
            where: { id: roomId },
            data: { updatedAt: new Date() },
          });

          // Broadcast message to room
          this.io.to(`chat:${roomId}`).emit("new-message", {
            message,
            roomId,
          });

          // Send notification to offline users
          await this.notifyOfflineUsers(roomId, message);
        } catch (error) {
          console.error("Chat message error:", error);
          socket.emit("error", { message: "Failed to send message" });
        }
      });

      // Handle typing indicators
      socket.on("typing-start", (data) => {
        const { roomId } = data;
        socket.to(`chat:${roomId}`).emit("user-typing", {
          userId: socket.user.id,
          username: socket.user.username,
          roomId,
        });
      });

      socket.on("typing-stop", (data) => {
        const { roomId } = data;
        socket.to(`chat:${roomId}`).emit("user-stopped-typing", {
          userId: socket.user.id,
          roomId,
        });
      });

      // Handle order status updates (admin only)
      socket.on("order-status-update", async (data) => {
        if (!["ADMIN", "MODERATOR"].includes(socket.user.role)) {
          return;
        }

        const { orderId, status } = data;

        try {
          const order = await prisma.order.update({
            where: { id: orderId },
            data: { status },
            include: {
              user: {
                select: { id: true, username: true },
              },
            },
          });

          // Notify the order owner
          this.io.to(`user:${order.user.id}`).emit("order-updated", {
            orderId,
            status,
            updatedAt: new Date(),
          });

          // Notify all admins
          this.io.to("admin").emit("order-status-changed", {
            orderId,
            status,
            updatedBy: socket.user.username,
            updatedAt: new Date(),
          });
        } catch (error) {
          console.error("Order status update error:", error);
          socket.emit("error", { message: "Failed to update order status" });
        }
      });

      // Handle stock updates (admin only)
      socket.on("stock-update", async (data) => {
        if (!["ADMIN", "MODERATOR"].includes(socket.user.role)) {
          return;
        }

        const { productId, newStock } = data;

        try {
          const product = await prisma.product.update({
            where: { id: productId },
            data: { stock: newStock },
          });

          // Notify all connected users about stock update
          this.io.emit("stock-updated", {
            productId,
            newStock,
            updatedBy: socket.user.username,
            updatedAt: new Date(),
          });

          // Send low stock alert if stock is low
          if (newStock <= 10) {
            this.io.to("admin").emit("low-stock-alert", {
              productId,
              productName: product.name,
              currentStock: newStock,
              timestamp: new Date(),
            });
          }
        } catch (error) {
          console.error("Stock update error:", error);
          socket.emit("error", { message: "Failed to update stock" });
        }
      });

      // Handle user status updates
      socket.on("update-status", async (data) => {
        const { isOnline } = data;

        try {
          await prisma.user.update({
            where: { id: socket.user.id },
            data: {
              isOnline,
              lastSeen: new Date(),
            },
          });

          // Broadcast status change
          socket.broadcast.emit("user-status-changed", {
            userId: socket.user.id,
            username: socket.user.username,
            isOnline,
            lastSeen: new Date(),
          });
        } catch (error) {
          console.error("Status update error:", error);
        }
      });

      // Handle disconnection
      socket.on("disconnect", async () => {
        console.log(`User disconnected: ${socket.user.username}`);

        // Update user offline status
        await this.updateUserOnlineStatus(socket.user.id, false);

        // Remove from connected users
        this.connectedUsers.delete(socket.user.id);

        // Notify others about user offline status
        socket.broadcast.emit("user-offline", {
          userId: socket.user.id,
          username: socket.user.username,
        });
      });
    });
  }

  // Update user online status in database
  async updateUserOnlineStatus(userId, isOnline) {
    try {
      await prisma.user.update({
        where: { id: userId },
        data: {
          isOnline,
          lastSeen: new Date(),
        },
      });
    } catch (error) {
      console.error("Update online status error:", error);
    }
  }

  // Notify offline users about new messages
  async notifyOfflineUsers(roomId, message) {
    try {
      const room = await prisma.chatRoom.findUnique({
        where: { id: roomId },
        include: {
          members: {
            include: {
              user: {
                select: {
                  id: true,
                  username: true,
                  isOnline: true,
                },
              },
            },
          },
        },
      });

      if (!room) return;

      // Send notifications to offline users
      room.members.forEach((member) => {
        if (!member.user.isOnline && member.user.id !== message.senderId) {
          this.io
            .to(`user:${member.user.id}`)
            .emit("new-message-notification", {
              roomId,
              message: {
                id: message.id,
                content: message.content,
                sender: message.sender,
              },
            });
        }
      });
    } catch (error) {
      console.error("Notify offline users error:", error);
    }
  }

  // Broadcast to all connected users
  broadcast(event, data) {
    if (this.io) {
      this.io.emit(event, data);
    }
  }

  // Send to specific user
  sendToUser(userId, event, data) {
    if (this.io) {
      this.io.to(`user:${userId}`).emit(event, data);
    }
  }

  // Send to admin users
  sendToAdmins(event, data) {
    if (this.io) {
      this.io.to("admin").emit(event, data);
    }
  }

  // Send to specific room
  sendToRoom(roomName, event, data) {
    if (this.io) {
      this.io.to(roomName).emit(event, data);
    }
  }

  // Send to chat room
  sendToChatRoom(roomId, event, data) {
    if (this.io) {
      this.io.to(`chat:${roomId}`).emit(event, data);
    }
  }

  // Get connected users count
  getConnectedUsersCount() {
    return this.connectedUsers.size;
  }

  // Get connected users list
  getConnectedUsers() {
    return Array.from(this.connectedUsers.values());
  }

  // Notify about new order (called from order controller)
  notifyNewOrder(order) {
    this.sendToAdmins("new-order", {
      orderId: order.id,
      userId: order.userId,
      total: order.total,
      createdAt: order.createdAt,
    });
  }

  // Notify about low stock
  async notifyLowStock(productId, currentStock) {
    this.sendToAdmins("low-stock-alert", {
      productId,
      currentStock,
      timestamp: new Date(),
    });
  }

  // Send chat notification
  sendChatNotification(userId, notification) {
    this.sendToUser(userId, "chat-notification", notification);
  }
}

module.exports = new SocketService();
