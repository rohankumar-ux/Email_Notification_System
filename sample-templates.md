# Simple Email Templates for Email Notification System

This document provides 2 simple, minimal email templates that you can add to your Email Notification System.

## Template 1: Simple Welcome Email

### Template Details
- **Name**: `simple_welcome`
- **Subject**: `Welcome {{name}}!`
- **Variable Keys**: `name,companyName`
- **HTML Body**:

```html
<h1>Welcome {{name}}!</h1>
<p>Thank you for joining {{companyName}}. Your account has been created successfully.</p>
<p>You can now log in and start using our services.</p>
<p>Best regards,<br>{{companyName}} Team</p>
```

### How to Add This Template:

1. Navigate to **Template Management** in your application
2. Click **"+ New Template"**
3. Fill in the form:
   - **Name**: `simple_welcome`
   - **Subject**: `Welcome {{name}}!`
   - **Variable Keys**: `name,companyName`
   - **HTML Body**: Copy and paste the HTML code above
4. Click **Create**

### How to Use This Template:

1. Go to **Send Template Email** page
2. Select the `simple_welcome` template
3. Fill in the template variables:
   - **{{name}}**: John Doe
   - **{{companyName}}**: TechCorp
4. Add recipient email(s)
5. Click **Send**

---

## Template 2: Simple Order Confirmation

### Template Details
- **Name**: `simple_order_confirmation`
- **Subject**: `Order #{{orderId}} Confirmed`
- **Variable Keys**: `orderId,customerName,totalAmount`
- **HTML Body**:

```html
<h2>Order Confirmation</h2>
<p>Dear {{customerName}},</p>
<p>Your order #{{orderId}} has been confirmed.</p>
<p>Total Amount: ${{totalAmount}}</p>
<p>Thank you for your purchase!</p>
<p>Best regards,<br>Customer Service Team</p>
```

### How to Add This Template:

1. Navigate to **Template Management** in your application
2. Click **"+ New Template"**
3. Fill in the form:
   - **Name**: `simple_order_confirmation`
   - **Subject**: `Order #{{orderId}} Confirmed`
   - **Variable Keys**: `orderId,customerName,totalAmount`
   - **HTML Body**: Copy and paste the HTML code above
4. Click **Create**

### How to Use This Template:

1. Go to **Send Template Email** page
2. Select the `simple_order_confirmation` template
3. Fill in the template variables:
   - **{{orderId}}**: ORD-1234
   - **{{customerName}}**: Sarah Johnson
   - **{{totalAmount}}**: 156.99
4. Add recipient email(s)
5. Click **Send**

---

## Quick Reference

### Variable Naming:
- Use simple, descriptive names (e.g., `name`, `orderId`, `totalAmount`)
- Separate variables with commas in the Variable Keys field

### Tips:
- Keep HTML minimal for better email client compatibility
- Only create variables for content that changes
- Test with different values before sending to recipients

These simple templates are ready to use and work with any email client!
