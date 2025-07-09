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

router.put(
  "/my-orders/:id/cancel",
  authenticateToken,
  orderController.cancelOrder
);
router.get("/my-orders/:id", authenticateToken, orderController.getOrder);
router.put(
  "/my-orders/:id/update",
  authenticateToken,
  orderController.updateOrderDetails
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
