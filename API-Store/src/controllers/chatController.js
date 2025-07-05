const { prisma } = require("../config/database");

// Create or get direct chat room between two users
const getOrCreateDirectRoom = async (req, res) => {
  try {
    const { userId } = req.params;
    const currentUserId = req.user.id;

    if (currentUserId === userId) {
      return res
        .status(400)
        .json({ error: "Cannot create room with yourself" });
    }

    // Check if direct room already exists
    let room = await prisma.chatRoom.findFirst({
      where: {
        type: "DIRECT",
        members: {
          every: {
            userId: {
              in: [currentUserId, userId],
            },
          },
        },
      },
      include: {
        members: {
          include: {
            user: {
              select: {
                id: true,
                username: true,
                avatar: true,
                isOnline: true,
              },
            },
          },
        },
        messages: {
          take: 50,
          orderBy: { createdAt: "desc" },
          include: {
            sender: {
              select: {
                id: true,
                username: true,
                avatar: true,
              },
            },
          },
        },
      },
    });

    if (!room) {
      // Create new direct room
      room = await prisma.chatRoom.create({
        data: {
          type: "DIRECT",
          members: {
            create: [{ userId: currentUserId }, { userId: userId }],
          },
        },
        include: {
          members: {
            include: {
              user: {
                select: {
                  id: true,
                  username: true,
                  avatar: true,
                  isOnline: true,
                },
              },
            },
          },
          messages: {
            take: 50,
            orderBy: { createdAt: "desc" },
            include: {
              sender: {
                select: {
                  id: true,
                  username: true,
                  avatar: true,
                },
              },
            },
          },
        },
      });
    }

    res.json({ room });
  } catch (error) {
    console.error("Get or create direct room error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Get user's chat rooms
const getUserRooms = async (req, res) => {
  try {
    const userId = req.user.id;
    const { page = 1, limit = 20 } = req.query;
    const skip = (parseInt(page) - 1) * parseInt(limit);

    const rooms = await prisma.chatRoom.findMany({
      where: {
        members: {
          some: {
            userId: userId,
          },
        },
      },
      include: {
        members: {
          include: {
            user: {
              select: {
                id: true,
                username: true,
                avatar: true,
                isOnline: true,
              },
            },
          },
        },
        messages: {
          take: 1,
          orderBy: { createdAt: "desc" },
          include: {
            sender: {
              select: {
                id: true,
                username: true,
              },
            },
          },
        },
        _count: {
          select: {
            messages: {
              where: {
                isRead: false,
                receiverId: userId,
              },
            },
          },
        },
      },
      skip,
      take: parseInt(limit),
      orderBy: {
        updatedAt: "desc",
      },
    });

    res.json({ rooms });
  } catch (error) {
    console.error("Get user rooms error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Get room messages
const getRoomMessages = async (req, res) => {
  try {
    const { roomId } = req.params;
    const userId = req.user.id;
    const { page = 1, limit = 50 } = req.query;
    const skip = (parseInt(page) - 1) * parseInt(limit);

    // Check if user is member of the room
    const membership = await prisma.chatRoomMember.findUnique({
      where: {
        roomId_userId: {
          roomId,
          userId,
        },
      },
    });

    if (!membership) {
      return res.status(403).json({ error: "Access denied" });
    }

    const messages = await prisma.message.findMany({
      where: { roomId },
      include: {
        sender: {
          select: {
            id: true,
            username: true,
            avatar: true,
          },
        },
      },
      skip,
      take: parseInt(limit),
      orderBy: { createdAt: "desc" },
    });

    // Mark messages as read
    await prisma.message.updateMany({
      where: {
        roomId,
        receiverId: userId,
        isRead: false,
      },
      data: { isRead: true },
    });

    res.json({ messages: messages.reverse() });
  } catch (error) {
    console.error("Get room messages error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Send message
const sendMessage = async (req, res) => {
  try {
    const { roomId } = req.params;
    const { content, type = "TEXT" } = req.body;
    const senderId = req.user.id;

    // Check if user is member of the room
    const membership = await prisma.chatRoomMember.findUnique({
      where: {
        roomId_userId: {
          roomId,
          userId: senderId,
        },
      },
    });

    if (!membership) {
      return res.status(403).json({ error: "Access denied" });
    }

    // Get room info for receiver
    const room = await prisma.chatRoom.findUnique({
      where: { id: roomId },
      include: {
        members: {
          where: {
            userId: { not: senderId },
          },
          include: {
            user: {
              select: {
                id: true,
                username: true,
              },
            },
          },
        },
      },
    });

    const message = await prisma.message.create({
      data: {
        roomId,
        senderId,
        receiverId: room.type === "DIRECT" ? room.members[0]?.userId : null,
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

    res.status(201).json({ message });
  } catch (error) {
    console.error("Send message error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Create support chat room (admin only)
const createSupportRoom = async (req, res) => {
  try {
    const { name, userId } = req.body;

    const room = await prisma.chatRoom.create({
      data: {
        name: name || `Support - ${new Date().toLocaleDateString()}`,
        type: "SUPPORT",
        members: {
          create: [
            { userId, role: "MEMBER" },
            { userId: req.user.id, role: "ADMIN" },
          ],
        },
      },
      include: {
        members: {
          include: {
            user: {
              select: {
                id: true,
                username: true,
                avatar: true,
                isOnline: true,
              },
            },
          },
        },
      },
    });

    res.status(201).json({ room });
  } catch (error) {
    console.error("Create support room error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Get all support rooms (admin only)
const getSupportRooms = async (req, res) => {
  try {
    const { page = 1, limit = 20 } = req.query;
    const skip = (parseInt(page) - 1) * parseInt(limit);

    const rooms = await prisma.chatRoom.findMany({
      where: {
        type: "SUPPORT",
      },
      include: {
        members: {
          include: {
            user: {
              select: {
                id: true,
                username: true,
                avatar: true,
                isOnline: true,
              },
            },
          },
        },
        messages: {
          take: 1,
          orderBy: { createdAt: "desc" },
          include: {
            sender: {
              select: {
                id: true,
                username: true,
              },
            },
          },
        },
        _count: {
          select: {
            messages: {
              where: {
                isRead: false,
              },
            },
          },
        },
      },
      skip,
      take: parseInt(limit),
      orderBy: {
        updatedAt: "desc",
      },
    });

    res.json({ rooms });
  } catch (error) {
    console.error("Get support rooms error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Update user online status
const updateOnlineStatus = async (req, res) => {
  try {
    const { isOnline } = req.body;
    const userId = req.user.id;

    await prisma.user.update({
      where: { id: userId },
      data: {
        isOnline,
        lastSeen: new Date(),
      },
    });

    res.json({ message: "Status updated successfully" });
  } catch (error) {
    console.error("Update online status error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

module.exports = {
  getOrCreateDirectRoom,
  getUserRooms,
  getRoomMessages,
  sendMessage,
  createSupportRoom,
  getSupportRooms,
  updateOnlineStatus,
};
