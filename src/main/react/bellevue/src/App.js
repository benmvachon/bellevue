import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './utils/AuthContext.js';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import ProfilePage from './pages/ProfilePage';
import RecipePage from './pages/RecipePage';

function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/profile/:id" element={<ProfilePage />} />
          <Route path="/recipe/:id" element={<RecipePage />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
