import { Outlet, Navigate } from "react-router-dom";

const UpdateProfileInfoRoutes = ({ isAuthenticated }) => {
    return isAuthenticated ? <Outlet /> : <Navigate to="/" />;
};

export default UpdateProfileInfoRoutes;
