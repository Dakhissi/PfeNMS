const { prisma } = require("../config/database");

// Get admin dashboard analytics
const getDashboardAnalytics = async (req, res) => {
  try {
    const [
      totalUsers,
      totalProducts,
      totalOrders,
      totalRevenue,
      recentOrders,
      lowStockProducts,
      onlineUsers,
    ] = await Promise.all([
      prisma.user.count(),
      prisma.product.count(),
      prisma.order.count(),
      prisma.order.aggregate({
        where: { status: { in: ["CONFIRMED", "SHIPPED", "DELIVERED"] } },
        _sum: { total: true },
      }),
      prisma.order.findMany({
        take: 10,
        orderBy: { createdAt: "desc" },
        include: {
          user: {
            select: { username: true, email: true },
          },
          items: {
            include: {
              product: {
                select: { name: true },
              },
            },
          },
        },
      }),
      prisma.product.findMany({
        where: { stock: { lte: 10 } },
        take: 10,
        orderBy: { stock: "asc" },
      }),
      prisma.user.count({
        where: { isOnline: true },
      }),
    ]);

    // Get monthly revenue for the last 6 months
    const sixMonthsAgo = new Date();
    sixMonthsAgo.setMonth(sixMonthsAgo.getMonth() - 6);

    const monthlyRevenue = await prisma.order.groupBy({
      by: ["createdAt"],
      where: {
        createdAt: { gte: sixMonthsAgo },
        status: { in: ["CONFIRMED", "SHIPPED", "DELIVERED"] },
      },
      _sum: { total: true },
    });

    res.json({
      analytics: {
        totalUsers,
        totalProducts,
        totalOrders,
        totalRevenue: totalRevenue._sum.total || 0,
        onlineUsers,
        recentOrders,
        lowStockProducts,
        monthlyRevenue,
      },
    });
  } catch (error) {
    console.error("Get dashboard analytics error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Get all users with pagination and filtering
const getAllUsers = async (req, res) => {
  try {
    const { page = 1, limit = 20, role, search } = req.query;
    const skip = (parseInt(page) - 1) * parseInt(limit);

    const where = {};
    if (role) where.role = role;
    if (search) {
      where.OR = [
        { username: { contains: search, mode: "insensitive" } },
        { email: { contains: search, mode: "insensitive" } },
      ];
    }

    const [users, total] = await Promise.all([
      prisma.user.findMany({
        where,
        skip,
        take: parseInt(limit),
        orderBy: { createdAt: "desc" },
        select: {
          id: true,
          email: true,
          username: true,
          role: true,
          isOnline: true,
          lastSeen: true,
          createdAt: true,
          _count: {
            select: { orders: true },
          },
        },
      }),
      prisma.user.count({ where }),
    ]);

    const totalPages = Math.ceil(total / parseInt(limit));

    res.json({
      users,
      pagination: {
        page: parseInt(page),
        limit: parseInt(limit),
        total,
        totalPages,
      },
    });
  } catch (error) {
    console.error("Get all users error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Update user role
const updateUserRole = async (req, res) => {
  try {
    const { id } = req.params;
    const { role } = req.body;

    const user = await prisma.user.update({
      where: { id },
      data: { role },
      select: {
        id: true,
        email: true,
        username: true,
        role: true,
        createdAt: true,
      },
    });

    res.json({
      message: "User role updated successfully",
      user,
    });
  } catch (error) {
    console.error("Update user role error:", error);
    if (error.code === "P2025") {
      return res.status(404).json({ error: "User not found" });
    }
    res.status(500).json({ error: "Internal server error" });
  }
};

// Update product stock
const updateProductStock = async (req, res) => {
  try {
    const { id } = req.params;
    const { stock, action = "set" } = req.body;

    let updateData = {};

    if (action === "increment") {
      updateData = { stock: { increment: parseInt(stock) } };
    } else if (action === "decrement") {
      updateData = { stock: { decrement: parseInt(stock) } };
    } else {
      updateData = { stock: parseInt(stock) };
    }

    const product = await prisma.product.update({
      where: { id },
      data: updateData,
    });

    res.json({
      message: "Product stock updated successfully",
      product,
    });
  } catch (error) {
    console.error("Update product stock error:", error);
    if (error.code === "P2025") {
      return res.status(404).json({ error: "Product not found" });
    }
    res.status(500).json({ error: "Internal server error" });
  }
};

// Bulk update product stock
const bulkUpdateProductStock = async (req, res) => {
  try {
    const { updates } = req.body;

    const results = await Promise.all(
      updates.map(async (update) => {
        try {
          const product = await prisma.product.update({
            where: { id: update.productId },
            data: { stock: parseInt(update.stock) },
          });
          return { success: true, product };
        } catch (error) {
          return {
            success: false,
            productId: update.productId,
            error: error.message,
          };
        }
      })
    );

    res.json({
      message: "Bulk stock update completed",
      results,
    });
  } catch (error) {
    console.error("Bulk update product stock error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Get order analytics
const getOrderAnalytics = async (req, res) => {
  try {
    const { period = "30" } = req.query;
    const daysAgo = new Date();
    daysAgo.setDate(daysAgo.getDate() - parseInt(period));

    const [
      totalOrders,
      pendingOrders,
      confirmedOrders,
      shippedOrders,
      deliveredOrders,
      cancelledOrders,
      totalRevenue,
      averageOrderValue,
    ] = await Promise.all([
      prisma.order.count({
        where: { createdAt: { gte: daysAgo } },
      }),
      prisma.order.count({
        where: {
          createdAt: { gte: daysAgo },
          status: "PENDING",
        },
      }),
      prisma.order.count({
        where: {
          createdAt: { gte: daysAgo },
          status: "CONFIRMED",
        },
      }),
      prisma.order.count({
        where: {
          createdAt: { gte: daysAgo },
          status: "SHIPPED",
        },
      }),
      prisma.order.count({
        where: {
          createdAt: { gte: daysAgo },
          status: "DELIVERED",
        },
      }),
      prisma.order.count({
        where: {
          createdAt: { gte: daysAgo },
          status: "CANCELLED",
        },
      }),
      prisma.order.aggregate({
        where: {
          createdAt: { gte: daysAgo },
          status: { in: ["CONFIRMED", "SHIPPED", "DELIVERED"] },
        },
        _sum: { total: true },
      }),
      prisma.order.aggregate({
        where: {
          createdAt: { gte: daysAgo },
          status: { in: ["CONFIRMED", "SHIPPED", "DELIVERED"] },
        },
        _avg: { total: true },
      }),
    ]);

    // Get daily order counts for the period
    const dailyOrders = await prisma.order.groupBy({
      by: ["createdAt"],
      where: { createdAt: { gte: daysAgo } },
      _count: { id: true },
    });

    res.json({
      analytics: {
        period: parseInt(period),
        totalOrders,
        pendingOrders,
        confirmedOrders,
        shippedOrders,
        deliveredOrders,
        cancelledOrders,
        totalRevenue: totalRevenue._sum.total || 0,
        averageOrderValue: averageOrderValue._avg.total || 0,
        dailyOrders,
      },
    });
  } catch (error) {
    console.error("Get order analytics error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Get product analytics
const getProductAnalytics = async (req, res) => {
  try {
    const [
      totalProducts,
      activeProducts,
      lowStockProducts,
      outOfStockProducts,
      topSellingProducts,
      categoryDistribution,
    ] = await Promise.all([
      prisma.product.count(),
      prisma.product.count({ where: { isActive: true } }),
      prisma.product.count({ where: { stock: { lte: 10, gt: 0 } } }),
      prisma.product.count({ where: { stock: 0 } }),
      prisma.orderItem.groupBy({
        by: ["productId"],
        _sum: { quantity: true },
        orderBy: { _sum: { quantity: true } },
        take: 10,
      }),
      prisma.product.groupBy({
        by: ["category"],
        _count: { id: true },
      }),
    ]);

    // Get top selling products with details
    const topProducts = await Promise.all(
      topSellingProducts.map(async (item) => {
        const product = await prisma.product.findUnique({
          where: { id: item.productId },
          select: { name: true, price: true, stock: true },
        });
        return {
          ...item,
          product,
        };
      })
    );

    res.json({
      analytics: {
        totalProducts,
        activeProducts,
        lowStockProducts,
        outOfStockProducts,
        topSellingProducts: topProducts,
        categoryDistribution,
      },
    });
  } catch (error) {
    console.error("Get product analytics error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Export data (orders, products, users)
const exportData = async (req, res) => {
  try {
    const { type, format = "json" } = req.query;

    let data;
    switch (type) {
      case "orders":
        data = await prisma.order.findMany({
          include: {
            user: { select: { username: true, email: true } },
            items: {
              include: {
                product: { select: { name: true } },
              },
            },
          },
          orderBy: { createdAt: "desc" },
        });
        break;
      case "products":
        data = await prisma.product.findMany({
          orderBy: { createdAt: "desc" },
        });
        break;
      case "users":
        data = await prisma.user.findMany({
          select: {
            id: true,
            email: true,
            username: true,
            role: true,
            createdAt: true,
          },
          orderBy: { createdAt: "desc" },
        });
        break;
      default:
        return res.status(400).json({ error: "Invalid export type" });
    }

    if (format === "csv") {
      // Convert to CSV format
      const csv = convertToCSV(data);
      res.setHeader("Content-Type", "text/csv");
      res.setHeader(
        "Content-Disposition",
        `attachment; filename=${type}_${
          new Date().toISOString().split("T")[0]
        }.csv`
      );
      return res.send(csv);
    }

    res.json({ data });
  } catch (error) {
    console.error("Export data error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
};

// Helper function to convert data to CSV
const convertToCSV = (data) => {
  if (!data.length) return "";

  const headers = Object.keys(data[0]);
  const csvRows = [headers.join(",")];

  for (const row of data) {
    const values = headers.map((header) => {
      const value = row[header];
      return typeof value === "string"
        ? `"${value.replace(/"/g, '""')}"`
        : value;
    });
    csvRows.push(values.join(","));
  }

  return csvRows.join("\n");
};

module.exports = {
  getDashboardAnalytics,
  getAllUsers,
  updateUserRole,
  updateProductStock,
  bulkUpdateProductStock,
  getOrderAnalytics,
  getProductAnalytics,
  exportData,
};
