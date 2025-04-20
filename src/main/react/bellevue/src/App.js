import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './utils/AuthContext.js';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import ProfilePage from './pages/ProfilePage';
import HomePage from './pages/HomePage';
import CategoryPage from './pages/CategoryPage.js';
import ForumPage from './pages/ForumPage.js';
import PostPage from './pages/PostPage.js';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/" element={<HomePage />} />
          <Route path="/profile/:id" element={<ProfilePage />} />
          <Route path="/category/:category" element={<CategoryPage />} />
          <Route path="/forum/:id" element={<ForumPage />} />
          <Route path="/post/:id" element={<PostPage />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
