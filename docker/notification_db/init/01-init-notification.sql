CREATE TABLE IF NOT EXISTS email_templates (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    subject_template VARCHAR(500) NOT NULL,
    body_template TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS email_notifications (
    id UUID PRIMARY KEY,
    recipient_email VARCHAR(150) NOT NULL,
    subject VARCHAR(500) NOT NULL,
    body TEXT NOT NULL,
    template_code VARCHAR(100),
    status VARCHAR(20) NOT NULL,
    sent_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    FOREIGN KEY (template_code) REFERENCES email_templates(code)
);

INSERT INTO email_templates (code, name, subject_template, body_template) VALUES
    ('ORDER_CONFIRMATION', 'Order Confirmation',
     'Order #${orderId} confirmed — thank you for your purchase!',
     'Hi ${username},\n\nThank you for your order #${orderId}!\n\nWe are processing it and will notify you when it ships.\n\nOrder total: ${orderTotal}\nShipping to: ${shippingAddress}\n\nBest regards,\nThe Marketplace Team'),

    ('SHIPPING_CONFIRMATION', 'Shipping Confirmation',
     'Order #${orderId} has been shipped!',
     'Hi ${username},\n\nGreat news! Your order #${orderId} is on its way.\n\nCarrier: ${carrier}\nTracking: ${trackingNumber}\nEstimated delivery: ${estimatedDelivery}\n\nTrack your package anytime.\n\nBest regards,\nThe Marketplace Team'),

    ('DELIVERY_CONFIRMATION', 'Delivery Confirmation',
     'Order #${orderId} has been delivered',
     'Hi ${username},\n\nYour order #${orderId} has been delivered.\n\nWe hope you love your items! If you have any issues, visit our Help Center.\n\nRate your purchase and help other buyers.\n\nBest regards,\nThe Marketplace Team'),

    ('WELCOME', 'Welcome to the Marketplace',
     'Welcome to our marketplace, ${username}!',
     'Hi ${username},\n\nWelcome to our marketplace! You are now part of a community of millions of buyers and sellers.\n\nGet started:\n- Complete your profile\n- Browse our categories\n- List your first item\n\nHappy shopping!\nThe Marketplace Team'),

    ('PASSWORD_RESET', 'Password Reset Request',
     'Reset your password',
     'Hi ${username},\n\nYou requested a password reset. Click the link below to set a new password:\n\n${resetLink}\n\nThis link expires in 60 minutes. If you did not request this, you can safely ignore this email.\n\nBest regards,\nThe Marketplace Team'),

    ('INVOICE', 'Payment Invoice',
     'Invoice for order #${orderId} — payment confirmed',
     'Hi ${username},\n\nThank you for your payment. Here is your invoice:\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━\nINVOICE\n━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\nOrder: #${orderId}\nAmount: ${orderTotal} ${currency}\nPayment method: ${paymentMethod}\nTransaction ID: ${transactionId}\nDate: ${paidAt}\n\n━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\nIf you have any questions, contact our support team.\n\nBest regards,\nThe Marketplace Team')
ON CONFLICT (code) DO NOTHING;
