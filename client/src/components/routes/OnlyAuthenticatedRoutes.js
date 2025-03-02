import { Outlet, Navigate } from "react-router-dom";
import { useAuth } from '../../AuthContext';


const OnlyAuthenticatedRoutes = () => {
    const { isAuthenticated } = useAuth();

    return isAuthenticated ? <Outlet /> : <Navigate to="/" />;
};

export default OnlyAuthenticatedRoutes;
