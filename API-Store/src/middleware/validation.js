const { body, param, query, validationResult } = require("express-validator");

// Handle validation errors
const handleValidationErrors = (req, res, next) => {
  const errors = validationResult(req);
  if (!errors.isEmpty()) {
    return res.status(400).json({
      error: "Validation failed",
      details: errors.array(),
    });
  }
  next();
};

// User validation rules
const validateUserRegistration = [
  body("email").isEmail().normalizeEmail(),
  body("username")
    .isLength({ min: 3, max: 30 })
    .matches(/^[a-zA-Z0-9_]+$/),
  body("password").isLength({ min: 6 }),
  handleValidationErrors,
];

const validateUserLogin = [
  body("email").isEmail().normalizeEmail(),
  body("password").notEmpty(),
  handleValidationErrors,
];

// Product validation rules
const validateProduct = [
  body("name").isLength({ min: 1, max: 100 }),
  body("price").isFloat({ min: 0 }),
  body("stock").isInt({ min: 0 }),
  body("category").isLength({ min: 1, max: 50 }),
  handleValidationErrors,
];

const validateProductUpdate = [
  body("name").optional().isLength({ min: 1, max: 100 }),
  body("price").optional().isFloat({ min: 0 }),
  body("stock").optional().isInt({ min: 0 }),
  body("category").optional().isLength({ min: 1, max: 50 }),
  handleValidationErrors,
];

// Order validation rules
const validateOrder = [
  body("items").isArray({ min: 1 }),
  body("items.*.productId").notEmpty(),
  body("items.*.quantity").isInt({ min: 1 }),
  handleValidationErrors,
];

// ID parameter validation
const validateId = [param("id").isLength({ min: 1 }), handleValidationErrors];

// Pagination validation
const validatePagination = [
  query("page").optional().isInt({ min: 1 }),
  query("limit").optional().isInt({ min: 1, max: 100 }),
  handleValidationErrors,
];

module.exports = {
  handleValidationErrors,
  validateUserRegistration,
  validateUserLogin,
  validateProduct,
  validateProductUpdate,
  validateOrder,
  validateId,
  validatePagination,
};
