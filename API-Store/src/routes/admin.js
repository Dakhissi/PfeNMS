const express = require("express");
const router = express.Router();
const adminController = require("../controllers/adminController");
const { authenticateToken, requireAdmin } = require("../middleware/auth");
const { validateId, validatePagination } = require("../middleware/validation");

// All routes require admin authentication
router.use(authenticateToken, requireAdmin);

// Dashboard and Analytics
router.get("/dashboard", adminController.getDashboardAnalytics);
router.get("/analytics/orders", adminController.getOrderAnalytics);
router.get("/analytics/products", adminController.getProductAnalytics);

// User Management
router.get("/users", validatePagination, adminController.getAllUsers);
router.put("/users/:id/role", validateId, adminController.updateUserRole);

// Product Management
router.put(
  "/products/:id/stock",
  validateId,
  adminController.updateProductStock
);
router.put("/products/bulk-stock", adminController.bulkUpdateProductStock);

// Data Export
router.get("/export", adminController.exportData);

module.exports = router;
