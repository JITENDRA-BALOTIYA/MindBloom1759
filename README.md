# MindBloom Admin Panel

A production-ready, modern admin dashboard for the MindBloom student mental health monitoring system. Built with React 18, TypeScript, Material-UI, Firebase, and beautiful micro-animations.

## 🧠 Overview

MindBloom Admin Panel is a professional SaaS-style administration interface designed to help educators and administrators monitor student mental health, attendance, engagement, and provide timely support interventions. The dashboard connects to a real-time Firebase Realtime Database to provide live updates on student metrics.

### Key Features

- **Real-time Dashboard**: Live statistics with animated counters
- **Student Management**: Complete student profiles with search, filtering, and CSV export
- **Attendance Monitoring**: Semester-wise attendance tracking with visual charts
- **Activity Analytics**: Comprehensive charts for mood checks, meditation, AI usage, and engagement
- **Mental Health Alerts**: Real-time high-risk student monitoring with contact options
- **Dark/Light Theme**: Smooth theme toggle with persistent user preference
- **Responsive Design**: Optimized for mobile, tablet, and desktop devices
- **Professional UI**: Glassmorphism cards, micro-animations, and consistent design language

## 🛠️ Tech Stack

- **React 18.3+** - UI framework with hooks
- **TypeScript** - Type safety and developer experience
- **Material-UI v5** - Component library and theming
- **Firebase v10** - Authentication and Realtime Database
- **react-router-dom v6.22+** - Client-side routing
- **react-chartjs-2 + Chart.js v4** - Interactive charts
- **framer-motion** - Micro-animations and transitions
- **date-fns** - Date formatting and manipulation
- **papaparse** - CSV export functionality
- **react-hot-toast** - Toast notifications
- **Vite** - Lightning-fast build tool

## 📋 Prerequisites

- Node.js 16+ and npm/yarn
- A Firebase project with Realtime Database enabled
- Google Chrome (recommended for development)

## 🚀 Installation & Setup

### 1. Clone and Install Dependencies

```bash
cd MindBloom
npm install --legacy-peer-deps
# or
yarn install
```

### 2. Configure Firebase

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a new project or use an existing one
3. Enable these services:
   - Authentication (Email/Password + Google Sign-in)
   - Realtime Database

4. Get your Firebase config:
   - Click "Project Settings" ⚙️
   - Copy the config object under "Web apps"

5. Create `.env` file in the root directory:

```bash
cp .env.example .env
```

6. Fill in your Firebase credentials in `.env`:

```env
VITE_FIREBASE_API_KEY=your_api_key
VITE_FIREBASE_AUTH_DOMAIN=your_project.firebaseapp.com
VITE_FIREBASE_DATABASE_URL=https://your_project.firebaseio.com
VITE_FIREBASE_PROJECT_ID=your_project
VITE_FIREBASE_STORAGE_BUCKET=your_project.appspot.com
VITE_FIREBASE_MESSAGING_SENDER_ID=your_messaging_sender_id
VITE_FIREBASE_APP_ID=your_app_id
```

### 3. Setup Firebase Database Structure

Create the following database structure in Firebase Realtime Database:

```
{
  "students": {
    "studentId1": {
      "id": "studentId1",
      "name": "John Doe",
      "email": "john@university.edu",
      "course": "Computer Science",
      "semester": 3,
      "enrollmentDate": 1694564400000,
      "lastActivity": 1710259200000,
      "profileImage": "https://..."
    }
  },
  "attendance": {
    "semester-3": {
      "studentId1": {
        "presentDays": 45,
        "absentDays": 5,
        "percentage": 90,
        "records": {
          "date1": { "status": "present", "timestamp": 1710259200000 }
        }
      }
    }
  },
  "activity": {
    "studentId1": {
      "week": {
        "moodChecks": [6, 7, 5, 8, 7, 6, 5],
        "meditationMinutes": [25, 30, 40, 30, 25, 15, 20],
        "aiAssistantUsage": [3, 5, 4, 6, 7, 2, 1],
        "engagementScore": 78,
        "dates": ["2024-03-04", "2024-03-05", ...]
      }
    }
  },
  "stressData": {
    "studentId1": {
      "currentStress": 45,
      "lastMoodCheck": 1710259200000,
      "riskLevel": "low"
    }
  }
}
```

### 4. Setup Firebase Authentication

1. Go to Firebase Console → Authentication
2. Enable "Email/Password" provider
3. Enable "Google" provider (optional but recommended)
4. Create a test admin account:
   - Email: `admin@mindbloom.com`
   - Password: `admin123456`

### 5. Setup Firebase Rules

Replace your Firestore/Database rules with (replace with your own for production):

```json
{
  "rules": {
    "students": {
      ".read": "auth != null && auth.token.role == 'admin'",
      ".write": "auth != null && auth.token.role == 'admin'"
    },
    "attendance": {
      ".read": "auth != null",
      ".write": "auth != null && auth.token.role == 'admin'"
    },
    "activity": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "stressData": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```

## 🏃 Running the Application

### Development Server

```bash
npm run dev
```

The app will open at `http://localhost:5173` with hot reload enabled.

### Production Build

```bash
npm run build
```

This creates an optimized production build in the `dist` folder.

### Preview Production Build

```bash
npm run preview
```

## 📁 Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── Logo.tsx
│   ├── Sidebar.tsx
│   ├── Navbar.tsx
│   ├── StatCard.tsx
│   ├── ChartCard.tsx
│   ├── ProtectedRoute.tsx
│   └── index.ts
├── pages/              # Page components
│   ├── LoginPage.tsx
│   ├── DashboardPage.tsx
│   ├── StudentsPage.tsx
│   ├── AttendancePage.tsx
│   ├── AnalyticsPage.tsx
│   └── AlertsPage.tsx
├── hooks/              # Custom React hooks
│   ├── useAuth.ts
│   └── index.ts
├── firebase/           # Firebase configuration
│   ├── config.ts
│   └── authService.ts
├── theme/              # MUI theme configuration
│   └── theme.ts
├── types/              # TypeScript type definitions
│   └── index.ts
├── App.tsx             # Main app component
└── main.tsx            # Entry point
```

## 🎨 Theme & Design

### Color Palette

- **Primary Teal**: `#00BFA5` - Main action color, calm and trustworthy
- **Secondary Violet**: `#7C3AED` - Accent color for secondary actions
- **Success Green**: `#10B981` - Positive states and achievements
- **Warning Yellow**: `#F59E0B` - Caution and attention needed
- **Error Red**: `#EF4444` - Critical alerts and errors
- **Dark Background**: `#0F172A` - Primary dark background
- **Dark Paper**: `#1E293B` - Secondary dark background

### Typography

- **"Inter"** - Body text, clean and modern
- **"Playfair Display"** - Headings, elegant and professional

### Design Features

- Glassmorphism: Semi-transparent cards with backdrop blur
- Smooth Animations: Powered by framer-motion
- Responsive Grid: MUI Grid system for perfect layouts
- Dark Mode by Default: Beautiful dark theme with light mode toggle

## 🔐 Security Features

- Firebase Authentication with Google Sign-in support
- Protected routes requiring authentication
- Real-time security rules in Firebase
- Secure environment variable management
- Session-based persistence

## 📊 Database Integration

The dashboard syncs in real-time with Firebase Realtime Database:

- **Students**: Main student registry with profiles
- **Attendance**: Semester-wise attendance tracking
- **Activity**: Weekly app usage metrics
- **Stress Data**: Current stress levels and risk assessment

All data updates are reflected instantly in the UI using Firebase listeners.

## 🎯 Key Pages

### Dashboard

- Real-time statistics with animated counters
- Weekly activity trend chart
- Stress level distribution doughnut chart
- Recent alerts section

### Students Management

- Full student database with advanced filtering
- Search by name or email
- Filter by semester
- Stress level color coding
- CSV export functionality
- Student detail modal

### Attendance Monitoring

- Semester selector tabs
- Attendance summary cards
- Present/Absent bar chart
- Detailed attendance records table
- Attendance insights

### Activity Analytics

- Mood check-in trends
- Meditation usage patterns
- AI assistant interaction tracking
- Overall engagement score
- Date range and student filtering
- Chart export options

### Mental Health Alerts

- Real-time high-risk student list
- Risk level color coding
- Quick contact buttons (Email/Phone)
- Risk level filtering and sorting
- Alert summary statistics

## 🚀 Deployment Options

### Vercel (Recommended)

```bash
npm install -g vercel
vercel
```

### Netlify

```bash
npm install -g netlify-cli
netlify deploy
```

### Docker

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps
COPY . .
RUN npm run build
EXPOSE 5173
CMD ["npm", "run", "preview"]
```

## 📱 Responsive Breakpoints

- **Mobile**: < 600px
- **Tablet**: 600px - 960px
- **Desktop**: > 960px

## 📝 Environment Variables

```env
VITE_FIREBASE_API_KEY          # Firebase API key
VITE_FIREBASE_AUTH_DOMAIN      # Firebase auth domain
VITE_FIREBASE_DATABASE_URL     # Firebase Realtime Database URL
VITE_FIREBASE_PROJECT_ID       # Firebase project ID
VITE_FIREBASE_STORAGE_BUCKET   # Firebase storage bucket
VITE_FIREBASE_MESSAGING_SENDER_ID  # Firebase messaging sender ID
VITE_FIREBASE_APP_ID           # Firebase app ID
```

## 🐛 Troubleshooting

### Firebase Connection Issues

- Verify all environment variables are correctly set
- Check Firebase rules allow admin access
- Ensure database is initialized with proper structure

### Chart Not Rendering

- Verify Chart.js and react-chartjs-2 are installed
- Clear browser cache and rebuild

### Styling Issues

- Ensure Material-UI is properly installed with `--legacy-peer-deps`
- Check theme provider wraps the entire app

### Authentication Issues

- Verify Firebase Authentication is enabled
- Check email/password provider is activated
- Test with demo credentials first

## 🧪 Demo Mode

The application includes demo data for testing:

**Demo Admin Login:**

- Email: `admin@mindbloom.com`
- Password: `admin123456`

Note: Demo data is simulated. Connect real Firebase for production use.

## 📚 Documentation

- [Material-UI Docs](https://mui.com/material-ui/)
- [Firebase Docs](https://firebase.google.com/docs)
- [React Router Docs](https://reactrouter.com/)
- [Framer Motion Docs](https://www.framer.com/motion/)
- [Chart.js Docs](https://www.chartjs.org/)

## 🤝 Contributing

To contribute improvements:

1. Create a feature branch
2. Make your changes with clear commit messages
3. Ensure all types are properly defined
4. Test thoroughly before submitting PR

## 📄 License

This project is proprietary software for the MindBloom mental health monitoring system.

## 👥 Support

For issues, feature requests, or questions:

- Create an issue in the repository
- Contact the development team
- Check existing documentation

## 🎉 Features Roadmap

- [ ] Advanced student filtering and search
- [ ] Custom report generation
- [ ] Intervention workflow management
- [ ] Student-teacher messaging
- [ ] Automated wellness alerts
- [ ] Data export with custom date ranges
- [ ] Admin role management
- [ ] Audit logs for admin actions
- [ ] Integration with academic systems
- [ ] Mobile app companion

---

**Made with 💙 for mental wellness**

MindBloom Admin Panel v1.0.0
