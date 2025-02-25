import { Outlet, Navigate } from "react-router-dom";
import { useState, useEffect } from "react";

const ProtectedAuthenticationRoutes = ({ isAuthenticated }) => {
    const [isProfileComplete, setIsProfileComplete] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (isAuthenticated) {
            fetch('http://localhost:8080/api/v1/auth/verify_status', {
                credentials: 'include'
            })
                .then(response => response.json())
                .then(verified => {
                    setIsProfileComplete(verified);
                    setIsLoading(false);
                })
                .catch(() => {
                    setIsProfileComplete(false);
                    setIsLoading(false);
                });
        } else {
            setIsLoading(false);
        }
    }, [isAuthenticated]);

    if (!isLoading) return !isAuthenticated || isProfileComplete ? <Outlet /> : <Navigate to="/update_profile" />;
};

export default ProtectedAuthenticationRoutes;
