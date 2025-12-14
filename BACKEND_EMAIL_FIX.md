# Backend Email Timeout Fix Guide

## 🔴 **Problem**
Backend error on Render.com: `✗ Email sending failed: Connection timeout`

This happens when your backend tries to send OTP/verification emails but the email service times out.

## ⚠️ **Why SMTP Doesn't Work on Render.com**

**SMTP (Gmail, Outlook, etc.) typically FAILS on Render.com and similar platforms because:**

1. 🚫 **Port Blocking**: Render.com blocks outgoing SMTP ports (25, 465, 587) to prevent spam abuse
2. 🔒 **Firewall Rules**: Strict security policies prevent SMTP connections
3. 🛡️ **Gmail Security**: Gmail blocks connections from unrecognized cloud IPs
4. ⏱️ **Connection Timeouts**: SMTP needs persistent connections that timeout on cloud platforms
5. 🌐 **No Static IP**: Your container gets random IPs that email providers block
6. 📧 **Spam Prevention**: Cloud provider IPs are often blacklisted by email services

**Result:** Even with correct credentials, SMTP will timeout or fail on Render.com.

## ✅ **THE SOLUTION: Use Email API Services Instead**

Instead of SMTP (which tries to connect directly), use HTTP-based email APIs:
- ✅ SendGrid (Recommended)
- ✅ Mailgun
- ✅ Amazon SES
- ✅ Postmark

These services use HTTP APIs (not SMTP ports), which work perfectly on all cloud platforms.

---

## ✅ **Solutions**

### **Solution 1: Switch to SendGrid (RECOMMENDED - Actually Works on Render!)** ⭐

SendGrid uses HTTP API instead of SMTP, which bypasses all the port blocking issues.

#### Why SendGrid Works When SMTP Doesn't:
- ✅ Uses HTTP/HTTPS (port 443) - never blocked
- ✅ No SMTP connection timeouts
- ✅ Works on all cloud platforms (Render, Heroku, Vercel, etc.)
- ✅ Free tier: 100 emails/day
- ✅ Better deliverability (99.9% inbox rate)
- ✅ Takes < 1 second to send emails

#### Step 1: Sign Up
1. Go to https://sendgrid.com/
2. Sign up for free account
3. Verify your email

#### Step 2: Get API Key
1. Go to Settings → API Keys
2. Create API Key
3. Copy the key (starts with `SG.`)

#### Step 3: Verify Sender Email
1. Go to Settings → Sender Authentication
2. Verify a single sender email (your email)
3. Check your email for verification link

#### Step 4: Install SendGrid in Backend
```bash
npm install @sendgrid/mail
```

#### Step 5: Update Backend Code
Replace Nodemailer with SendGrid:

```javascript
const sgMail = require('@sendgrid/mail');
sgMail.setApiKey(process.env.SENDGRID_API_KEY);

// Function to send OTP email
async function sendOtpEmail(toEmail, otpCode) {
  const msg = {
    to: toEmail,
    from: process.env.SENDGRID_FROM_EMAIL, // Must be verified sender
    subject: 'Votre code de vérification Karhebti',
    text: `Votre code OTP est: ${otpCode}`,
    html: `
      <div style="font-family: Arial, sans-serif; padding: 20px;">
        <h2>Bienvenue chez Karhebti!</h2>
        <p>Votre code de vérification est:</p>
        <h1 style="color: #4CAF50; font-size: 32px;">${otpCode}</h1>
        <p>Ce code expire dans 10 minutes.</p>
      </div>
    `,
  };

  try {
    await sgMail.send(msg);
    console.log('✅ Email sent successfully to:', toEmail);
    return { success: true };
  } catch (error) {
    console.error('❌ Email sending error:', error);
    return { success: false, error: error.message };
  }
}
```

#### Step 6: Add Environment Variables to Render
```
SENDGRID_API_KEY=SG.xxxxxxxxxxxxxxxxxxxxx
SENDGRID_FROM_EMAIL=your-verified-email@example.com
```

---

### **Solution 2: Fix Gmail SMTP Configuration (If Using Gmail)**

Gmail requires special configuration for third-party apps.

#### Step 1: Generate Gmail App Password
1. Go to your Google Account: https://myaccount.google.com/
2. Click **Security** → Enable **2-Step Verification** (if not already enabled)
3. Go back to Security → Click **App passwords**
4. Select **Mail** and **Other (Custom name)**
5. Name it "Karhebti Backend"
6. Copy the 16-character password

#### Step 2: Update Render Environment Variables
Go to your Render.com dashboard → Your Service → Environment:

```
EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_SECURE=false
EMAIL_USER=your-email@gmail.com
EMAIL_PASS=xxxx xxxx xxxx xxxx  (the 16-char app password)
EMAIL_TIMEOUT=60000
```

#### Step 3: Update Backend Code
If using Nodemailer, ensure timeout is set:

```javascript
const transporter = nodemailer.createTransport({
  host: process.env.EMAIL_HOST,
  port: parseInt(process.env.EMAIL_PORT),
  secure: false, // true for 465, false for other ports
  connectionTimeout: 60000, // 60 seconds
  greetingTimeout: 30000,
  socketTimeout: 60000,
  auth: {
    user: process.env.EMAIL_USER,
    pass: process.env.EMAIL_PASS
  }
});
```

---

### **Solution 3: Use Mailtrap for Testing (Development Only)**

Perfect for testing without sending real emails.

#### Setup:
1. Sign up at https://mailtrap.io/
2. Get SMTP credentials
3. Add to Render environment:

```
EMAIL_HOST=smtp.mailtrap.io
EMAIL_PORT=2525
EMAIL_USER=your-mailtrap-username
EMAIL_PASS=your-mailtrap-password
```

**Note:** Emails won't be delivered to real inboxes, only visible in Mailtrap inbox.

---

### **Solution 4: Temporary Bypass (For Testing Only)**

If you need to test other features without email, temporarily skip email sending:

```javascript
// In your signup route
async function handleSignup(req, res) {
  const { email, nom, prenom, motDePasse, telephone } = req.body;
  
  // Create user in database
  const user = await User.create({ email, nom, prenom, motDePasse, telephone });
  
  // Generate OTP
  const otpCode = generateOTP();
  await saveOTP(email, otpCode);
  
  // TEMPORARY: Log OTP instead of sending email
  console.log(`🔑 OTP for ${email}: ${otpCode}`);
  
  // Skip email sending for now
  // await sendOtpEmail(email, otpCode);
  
  res.json({ 
    message: 'Signup initiated. Check server logs for OTP.',
    success: true 
  });
}
```

**Important:** This is ONLY for testing! Remove for production.

---

## 🧪 **Testing the Fix**

### 1. Check Render Logs
```bash
# In Render dashboard, go to your service → Logs
# Look for:
✅ Email sent successfully
# Or:
❌ Email sending failed
```

### 2. Test Signup Flow
1. Try signup in your Android app
2. Check email inbox (or Mailtrap inbox)
3. Enter OTP code
4. Should complete successfully

### 3. Monitor Response Times
- Gmail SMTP: 2-5 seconds
- SendGrid API: < 1 second
- Mailtrap: 1-2 seconds

---

## 📋 **Comparison Table**

| Solution | Speed | Reliability | Free Tier | Best For |
|----------|-------|-------------|-----------|----------|
| **Gmail SMTP** | Slow (2-5s) | Medium | 500/day | Small apps |
| **SendGrid** | Fast (<1s) | High | 100/day | Production ⭐ |
| **Mailtrap** | Fast (1-2s) | High | 500/month | Testing only |

---

## 🎯 **Recommended Action Plan**

1. **Immediate Fix** (5 minutes):
   - Add Gmail app password to Render environment variables
   - Restart your Render service
   - Test signup

2. **Better Solution** (15 minutes):
   - Sign up for SendGrid
   - Get API key and verify sender
   - Update backend code to use SendGrid
   - Deploy to Render
   - Much more reliable! ⭐

3. **For Testing** (Optional):
   - Use Mailtrap for development
   - Switch to SendGrid for production

---

## 🆘 **Still Having Issues?**

### Check Backend Logs
Look for specific error messages:
- `ETIMEDOUT` → Firewall or network issue
- `Invalid login` → Wrong credentials
- `535 5.7.8` → Gmail needs app password
- `Connection refused` → Wrong port/host

### Common Render.com Issues
- ✅ Make sure all environment variables are set
- ✅ Restart service after adding variables
- ✅ Check if Render has network restrictions
- ✅ Verify email credentials are correct

---

**Last Updated**: December 14, 2025  
**Recommended**: Use SendGrid for production
