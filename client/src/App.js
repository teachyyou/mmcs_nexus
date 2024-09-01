import React, {useEffect, useState} from 'react';
import {BrowserRouter as Router, Route, Routes} from 'react-router-dom';
import LoginPage from './components/login/LoginPage';
import HomePage from './components/home/HomePage';
import './App.css';
import UpdateProfilePage from "./components/login/UpdateProfilePage";
import ProtectedAuthenticationRoutes from './components/routes/ProtectedAuthenticationRoutes';
import OnlyAuthenticatedRoutes from "./components/routes/OnlyAuthenticatedRoutes";
import UsersList from "./components/admin/UsersList";
import AuthenticatedAndVerifiedRoutes from "./components/routes/AuthenticatedAndVerifiedRoutes";


function App() {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetch('http://localhost:8080/api/v1/auth/status', {
            credentials: 'include'
        })
            .then(response => response.json())
            .then(data => {
                setIsAuthenticated(data.isAuthenticated);
                setIsLoading(false);
            });
    }, []);

    if (!isLoading) return (
        <Router>
            <Routes>

                //can get there only when not-authenticated OR updated user data
                <Route element={<ProtectedAuthenticationRoutes isAuthenticated={isAuthenticated}/>}>
                    <Route path="/" element={<HomePage isAuthenticated={isAuthenticated} setIsAuthenticated={setIsAuthenticated} />} />
                    <Route path="/login" element={<LoginPage />} />
                </Route>

                //can get here only when authenticated and updated user data
                <Route element={<AuthenticatedAndVerifiedRoutes isAuthenticated={isAuthenticated}/>}>
                    <Route path="/users" element={<UsersList />} />
                </Route>

                //can get here only when authenticated
                <Route element={<OnlyAuthenticatedRoutes isAuthenticated={isAuthenticated}/>}>
                    <Route path="/update-profile" element={<UpdateProfilePage />} />
                </Route>

            </Routes>
        </Router>
    );

}

export default App;
