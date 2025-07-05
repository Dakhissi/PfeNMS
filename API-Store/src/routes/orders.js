const express = require("express");
const router = express.Router();
const orderController = require("../controllers/orderController");
const { authenticateToken, requireAdmin } = require("../middleware/auth");
const {
  validateOrder,
  validateId,
  validatePagination,
} = require("../middleware/validation");

// User routes
router.post("/", authenticateToken, validateOrder, orderController.createOrder);
router.get(
  "/my-orders",
  authenticateToken,
  validatePagination,
  orderController.getUserOrders
);
router.get(
  "/my-orders/:id",
  authenticateToken,
  validateId,
  orderController.getOrder
);
router.put(
  "/my-orders/:id/cancel",
  authenticateToken,
  validateId,
  orderController.cancelOrder
);

// Admin routes
router.get(
  "/all",
  authenticateToken,
  requireAdmin,
  validatePagination,
  orderController.getAllOrders
);
router.put(
  "/:id/status",
  authenticateToken,
  requireAdmin,
  validateId,
  orderController.updateOrderStatus
);

module.exports = router;
