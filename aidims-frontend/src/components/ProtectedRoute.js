import React from 'react';
import { Navigate } from 'react-router-dom';
import { authService } from '../services/authService';

export const ProtectedRoute = ({ children, allowedRoles }) => {
  const user = authService.getCurrentUser();

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles && !allowedRoles.includes(user.role)) {
    switch (user.role) {
      case 'admin':
        return <Navigate to="/IndexAdmin" replace />;
      case 'doctor':
        return <Navigate to="/IndexDoctor" replace />;
      case 'receptionist':
        return <Navigate to="/IndexReceptionist" replace />;
      case 'technician':
        return <Navigate to="/IndexTechnician" replace />;
      default:
        return <Navigate to="/login" replace />;
    }
  }

  return children;
};

export const PublicRoute = ({ children, restrictedRole }) => {
  const user = authService.getCurrentUser();

  if (user && user.role === restrictedRole) {
    switch (user.role) {
      case 'admin':
        return <Navigate to="/IndexAdmin" replace />;
      case 'doctor':
        return <Navigate to="/IndexDoctor" replace />;
      case 'receptionist':
        return <Navigate to="/IndexReceptionist" replace />;
      case 'technician':
        return <Navigate to="/IndexTechnician" replace />;
    }
  }

  return children;
};
