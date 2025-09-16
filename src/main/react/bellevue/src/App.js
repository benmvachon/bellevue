import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './utils/AuthContext.js';
import { LocationProvider } from './utils/LocationContext.js';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import ProfilePage from './pages/ProfilePage';
import ForumPage from './pages/ForumPage.js';
import PostPage from './pages/PostPage.js';
import ForumMapPage from './pages/ForumMapPage.js';
import FriendsMapPage from './pages/FriendsMapPage.js';
import SuggestedFriendsMapPage from './pages/SuggestedFriendsMapPage.js';
import FriendsOfFriendsMapPage from './pages/FriendsOfFriendsMapPage.js';
import ErrorPage from './pages/ErrorPage.js';

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
            <Route path="/map/town" element={<ForumMapPage />} />
            <Route path="/map/neighborhood" element={<FriendsMapPage />} />
            <Route path="/map/suburbs" element={<SuggestedFriendsMapPage />} />
            <Route
              path="/map/neighborhood/:id"
              element={<FriendsOfFriendsMapPage />}
            />
            <Route path="/*" element={<ErrorPage />} />
          </Routes>
        </LocationProvider>
      </Router>
    </AuthProvider>
  );
}

App.displayName = 'Blorvis';

export default App;
