import React, { createContext, useState, useContext } from 'react';

const AuthContext = createContext({
    isAuthenticated: false,
    userStatus: null,
    userRole: null,
    userId: null,
    user: null,
    setIsAuthenticated: () => {},
    setUserStatus: () => {},
    setUserRole: () => {},
    setUserId: () => {},
    setUser: () => {}
});


export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [userRole, setUserRole] = useState(null);
    const [userStatus, setUserStatus] = useState(null);
    const [userId, setUserId] = useState(null);
    const [user, setUser] = useState(null);

    return (
        <AuthContext.Provider value={{isAuthenticated, userStatus, userRole, userId, user, setIsAuthenticated, setUserStatus, setUserRole, setUserId, setUser}}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);


export const useIsAdmin = () => {
    const { userRole } = useAuth();
    return userRole === "ROLE_ADMIN";
};

export const useIsJury = () => {
    const { userRole } = useAuth();
    return userRole === "ROLE_ADMIN" || userRole === "ROLE_JURY";
};

export const useIsUser = () => {
    const { userRole } = useAuth();
    return userRole === "ROLE_ADMIN" || userRole === "ROLE_JURY" || userRole === "ROLE_USER";
};

export default AuthContext;
