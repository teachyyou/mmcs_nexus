import { Outlet, Navigate } from "react-router-dom";

const OnlyAuthenticatedRoutes = ({ isAuthenticated }) => {
    return isAuthenticated ? <Outlet /> : <Navigate to="/" />;
};

export default OnlyAuthenticatedRoutes;
