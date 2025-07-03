import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './utils/AuthContext.js';
import { LocationProvider } from './utils/LocationContext.js';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import ProfilePage from './pages/ProfilePage';
import ForumPage from './pages/ForumPage.js';
import PostPage from './pages/PostPage.js';
import MapPage from './pages/MapPage.js';

function App() {
  return (
    <AuthProvider>
      <Router>
        <LocationProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/" element={<ForumPage />} />
            <Route path="/home/:id" element={<ProfilePage />} />
            <Route path="/town/:id" element={<ForumPage />} />
            <Route path="/flyer/:id" element={<PostPage />} />
            <Route path="/map/:section" element={<MapPage />} />
          </Routes>
        </LocationProvider>
      </Router>
    </AuthProvider>
  );
}

App.displayName = 'Blorvis';

export default App;
