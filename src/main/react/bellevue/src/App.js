import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './utils/AuthContext.js';
import { LocationProvider } from './utils/LocationContext.js';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import ProfilePage from './pages/ProfilePage';
import ForumPage from './pages/ForumPage.js';
import PostPage from './pages/PostPage.js';

function App() {
  return (
    <AuthProvider>
      <Router>
        <LocationProvider>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/" element={<ForumPage />} />
            <Route path="/profile/:id" element={<ProfilePage />} />
            <Route path="/forum/:id" element={<ForumPage />} />
            <Route path="/post/:id" element={<PostPage />} />
          </Routes>
        </LocationProvider>
      </Router>
    </AuthProvider>
  );
}

App.displayName = 'Blorvis';

export default App;
