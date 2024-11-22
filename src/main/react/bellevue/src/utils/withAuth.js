import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from './AuthContext';

const withAuth = (WrappedComponent) => {
  const ProtectedComponent = (props) => {
    const { isAuthenticated } = useAuth();

    if (!isAuthenticated) {
      return <Navigate to="/login" replace />;
    }

    return <WrappedComponent {...props} />;
  };

  ProtectedComponent.displayName = `withAuth(${WrappedComponent.displayName || WrappedComponent.name || 'Component'})`;

  return ProtectedComponent;
};

export default withAuth;
