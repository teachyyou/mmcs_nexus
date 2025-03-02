import { Outlet, Navigate } from "react-router-dom";
import { useAuth } from '../../AuthContext';

const ProtectedAuthenticationRoutes = () => {
    const {isAuthenticated, userStatus } = useAuth();


    return !isAuthenticated || userStatus==="VERIFIED" ? <Outlet /> : <Navigate to="/update_profile" />;
};

export default ProtectedAuthenticationRoutes;
