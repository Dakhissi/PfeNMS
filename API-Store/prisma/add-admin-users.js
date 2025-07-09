const { PrismaClient } = require("@prisma/client");
const bcrypt = require("bcryptjs");

const prisma = new PrismaClient();

async function addAdminUsers() {
  console.log("🔧 Adding admin users to database...");

  try {
    // Create admin user with simple credentials
    const adminPassword = await bcrypt.hash("admin", 12);
    const admin = await prisma.user.upsert({
      where: { email: "admin@default.com" },
      update: {},
      create: {
        email: "admin@default.com",
        username: "admin",
        password: adminPassword,
        role: "ADMIN",
        avatar:
          "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face",
      },
    });
    console.log("✅ Admin user created:", admin.email);

    // Create super admin user
    const superAdminPassword = await bcrypt.hash("superadmin", 12);
    const superAdmin = await prisma.user.upsert({
      where: { email: "superadmin@store.com" },
      update: {},
      create: {
        email: "superadmin@store.com",
        username: "superadmin",
        password: superAdminPassword,
        role: "ADMIN",
        avatar:
          "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop&crop=face",
      },
    });
    console.log("✅ Super admin user created:", superAdmin.email);

    console.log("🎉 All admin users added successfully!");
  } catch (error) {
    console.error("❌ Error adding admin users:", error);
  } finally {
    await prisma.$disconnect();
  }
}

// Run the function
addAdminUsers();
