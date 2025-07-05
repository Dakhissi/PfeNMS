const express = require("express");
const router = express.Router();
const chatController = require("../controllers/chatController");
const { authenticateToken, requireAdmin } = require("../middleware/auth");
const { validateId, validatePagination } = require("../middleware/validation");

// User routes
router.get(
  "/rooms",
  authenticateToken,
  validatePagination,
  chatController.getUserRooms
);
router.get(
  "/rooms/:roomId/messages",
  authenticateToken,
  validateId,
  validatePagination,
  chatController.getRoomMessages
);
router.post(
  "/rooms/:roomId/messages",
  authenticateToken,
  validateId,
  chatController.sendMessage
);
router.get(
  "/direct/:userId",
  authenticateToken,
  validateId,
  chatController.getOrCreateDirectRoom
);
router.put("/status", authenticateToken, chatController.updateOnlineStatus);

// Admin routes
router.post(
  "/support",
  authenticateToken,
  requireAdmin,
  chatController.createSupportRoom
);
router.get(
  "/support",
  authenticateToken,
  requireAdmin,
  validatePagination,
  chatController.getSupportRooms
);

module.exports = router;
