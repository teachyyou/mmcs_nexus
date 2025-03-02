import React from 'react';
import { Outlet, Navigate } from 'react-router-dom';
import { useAuth, useIsAdmin, useIsJury, useIsUser } from '../../AuthContext';

const AuthenticatedAndVerifiedRoutes = ({ roleRequired }) => {
    const { isAuthenticated, userStatus } = useAuth();
    const isAdmin = useIsAdmin();
    const isJury = useIsJury();
    const isUser = useIsUser();

    let hasRequiredRole = false;
    if (roleRequired === "ROLE_ADMIN") {
        hasRequiredRole = isAdmin;
    } else if (roleRequired === "ROLE_JURY") {
        hasRequiredRole = isJury;
    } else if (roleRequired === "ROLE_USER") {
        hasRequiredRole = isUser;
    }

    return isAuthenticated && userStatus === "VERIFIED" && hasRequiredRole ? (
        <Outlet />
    ) : (
        <Navigate to="/" />
    );
};

export default AuthenticatedAndVerifiedRoutes;
