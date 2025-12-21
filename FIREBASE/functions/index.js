/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

/// Firestore trigger for new orders
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();

// 如果沒有 customer email，用這個預設信箱（可改）
const DEFAULT_NOTIFY_EMAIL = "kingdom0916@gmail.com";

exports.createEmailDocument = onDocumentCreated("order/{orderId}", async (event) => {
  const snapshot = event.data;
  const order = snapshot ? (typeof snapshot.data === "function" ? snapshot.data() : snapshot) : null;

  // 取得 orderId：優先取 snapshot.id；若沒有就試 event?.params?.orderId；最後 fallback 'unknown'
  const orderId = (snapshot && snapshot.id) || (event && event?.params && event.params.orderId) || (order && order.id) || "unknown";

  if (!order) {
    console.log("🔴 No order data found.");
    return;
  }

  console.log("🟢 New order received:", order);

  try {
    // Fetch customer details
    const customerId = order.customerId || order.customer_id;
    let customer = null;
    if (customerId) {
      const customerDoc = await db.collection("customers").doc(customerId).get();
      customer = customerDoc.exists ? customerDoc.data() : null;

      if (!customer) {
        console.warn(`🔴 Customer with ID ${customerId} not found.`);
      }
    } else {
      console.warn("🔴 Order has no customerId.");
    }

    // Fetch product details
    const items = Array.isArray(order.items) ? order.items : [];
    const productPromises = items.map(async (item) => {
      const pid = item.productId || item.product_id;
      if (!pid) {
        console.warn("🔴 Item missing productId:", item);
        return null;
      }
      const productDoc = await db.collection("product").doc(pid).get();

      if (!productDoc.exists) {
        console.warn(`🔴 Product with ID ${pid} not found.`);
        return null;
      }

      return productDoc.data();
    });

    const productDetails = await Promise.all(productPromises);

    // Enhance order items with product data (safely)
    const cartItemsHtml = items
      .map((item, index) => {
        const product = productDetails[index];
        const title = product && product.title ? String(product.title).toUpperCase() : "Unknown Product";
        const flavor = item.flavor || item.flavors || "No Flavor";

        // safe price extraction
        let priceNum = 0;
        if (product && product.price != null) {
          const p = Number(product.price);
          priceNum = isNaN(p) ? 0 : p;
        }

        const qty = item.quantity != null ? item.quantity : 1;

        return `<li>
            <strong>${title}</strong>
            (${flavor}) - $${priceNum.toFixed(2)} x${qty}
          </li>`;
      })
      .join("");

    const paymentMethod = order.token ? `PAYPAL付款 (${order.token})` : "貨到付款";

    // Total amount safe parsing
    let totalAmount = 0;
    if (order.totalAmount != null) totalAmount = Number(order.totalAmount);
    else if (order.total_amount != null) totalAmount = Number(order.total_amount);
    if (isNaN(totalAmount)) totalAmount = 0;

    // 收件人：以 customer.email 為主，若沒有則 fallback 到 DEFAULT_NOTIFY_EMAIL
    let toEmails = [DEFAULT_NOTIFY_EMAIL];
    if (customer && customer.email) {
      // 如果 customer.email 是字串或陣列，處理兩種情況
      if (Array.isArray(customer.email)) {
        toEmails = customer.email.length ? customer.email : toEmails;
      } else if (typeof customer.email === "string" && customer.email.trim() !== "") {
        toEmails = [customer.email.trim()];
      }
    } else {
      console.warn("⚠️ customer.email not found — using default notify email.");
    }

    const emailData = {
      to: toEmails,
      message: {
        subject: `🎉 您的訂單已成立 (${orderId})`,
        html: `
          <h2>🛒 購物清單:</h2>
          <ul>${cartItemsHtml || "<li>No items</li>"}</ul>
          <h2>💰 消費金額:</h2>
          <p><strong>總金額:</strong> $${Number(totalAmount).toFixed(2)}</p>
          <h2>💳 付款方式:</h2>
          <p><strong>${paymentMethod}</strong></p>
          <h2>👋 收件人資訊:</h2>
            <p><strong>收件人:</strong> ${customer && customer.consigneeInfo ? customer.consigneeInfo.name : "N/A"}</p>
            <p><strong>電子信箱:</strong> ${customer ? customer.email : "N/A"}</p>
            <p><strong>地址:</strong> ${customer && customer.consigneeInfo ? `${customer.consigneeInfo.postalCode} ${customer.consigneeInfo.city} ${customer.consigneeInfo.address}` : "N/A"}</p>
            <p><strong>電話:</strong> ${customer && customer.consigneeInfo && customer.consigneeInfo.phone ? `+${customer.consigneeInfo.phone.dialCode} ${customer.consigneeInfo.phone.number}` : "N/A"}</p>
        `,
      },
    };

    // Add the email request to the mail collection
    await db.collection("mail").add(emailData);
    console.log("🟢 Mail document added to the collection successfully. To:", toEmails);
  } catch (error) {
    console.error("🔴 Error while trying to create a new mail document:", error);
  }

  return null;
});
