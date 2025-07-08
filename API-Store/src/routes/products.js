const express = require("express");
const router = express.Router();
const productController = require("../controllers/productController");
const {
  authenticateToken,
  requireAdmin,
  optionalAuth,
} = require("../middleware/auth");
const {
  validateProduct,
  validateProductUpdate,
  validateId,
  validatePagination,
} = require("../middleware/validation");

// Public routes (with optional auth for analytics)
router.get(
  "/",
  optionalAuth,
  validatePagination,
  productController.getProducts
);
router.get("/categories", productController.getCategories);
router.get("/:id", validateId, productController.getProduct);

// Admin only routes
router.post(
  "/",
  authenticateToken,
  requireAdmin,
  validateProduct,
  productController.createProduct
);
router.put(
  "/:id",
  authenticateToken,
  requireAdmin,
  validateId,
  validateProductUpdate,
  productController.updateProduct
);
router.delete(
  "/:id",
  authenticateToken,
  requireAdmin,
  validateId,
  productController.deleteProduct
);

module.exports = router;
