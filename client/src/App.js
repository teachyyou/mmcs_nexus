import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { AuthProvider, useAuth } from './AuthContext';
import LoginPage from './components/login/LoginPage';
import HomePage from './components/home/HomePage';
import './App.css';
import UpdateProfilePage from "./components/login/UpdateProfilePage";
import ProtectedAuthenticationRoutes from './components/routes/ProtectedAuthenticationRoutes';
import OnlyAuthenticatedRoutes from "./components/routes/OnlyAuthenticatedRoutes";
import AuthenticatedAndVerifiedRoutes from "./components/routes/AuthenticatedAndVerifiedRoutes";
import UserList from "./components/admin/userlist/UserList";
import UserEdit from "./components/admin/userlist/UserEdit";
import {Admin, Resource} from "react-admin";
import springBootRestProvider from "./components/admin/restProviders/springBootRestProvider";
import ProjectList from "./components/admin/project/ProjectList";
import {ProjectCreate, ProjectEdit} from "./components/admin/project/ProjectCE";
import EventList from "./components/admin/event/EventList";
import {EventCreate, EventEdit} from "./components/admin/event/EventCE";
import ProjectJuryManagement from "./components/admin/ProjectJuryManagement";
import ProjectEventManagement from "./components/admin/ProjectEventManagement";
import GradeTablePage from "./components/jury/GradeTablePage";
import AdminLayout from "./components/admin/AdminLayout";

const dataProvider = springBootRestProvider('http://localhost:8080/api/v1/admin');

function AppContent() {
    const { setIsAuthenticated, setUserStatus, setUserRole, setUser } = useAuth();
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetch('http://localhost:8080/api/v1/auth/status', {
            credentials: 'include'
        })
            .then(response => response.json())
            .then(data => {
                setIsAuthenticated(data.isAuthenticated);
                setUserRole(data.userRole);
                setUserStatus(data.userStatus);

                if (data.user) {
                    setUser({
                        login: data.user.login,
                        github_name: data.user.github_name,
                        firstname: data.user.firstname,
                        lastname: data.user.lastname,
                        avatarUrl: data.user.avatar_url,
                        email: data.user.email,
                        course: data.user.course,
                        group: data.user.group,
                    });
                }

                setIsLoading(false);
            });
    }, [setIsAuthenticated, setUserStatus, setUserRole]);

    if (isLoading) {
        return <div>Loading...</div>;
    }

    return (
        <Router>
            <Routes>
                {/* Доступны для не-аутентифицированных или обновивших профиль */}
                <Route element={<ProtectedAuthenticationRoutes/>}>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/login" element={<LoginPage />} />
                </Route>

                {/* Доступны для аутентифицированных и верифицированных (ТОЛЬКО ЖЮРИ) */}
                <Route element={<AuthenticatedAndVerifiedRoutes roleRequired={"ROLE_JURY"}/>}>
                    <Route path="/grades" element={<GradeTablePage />} />

                </Route>
                {/* Доступны для аутентифицированных и верифицированных (ТОЛЬКО АДМИНЫ)*/}
                <Route element={<AuthenticatedAndVerifiedRoutes roleRequired={"ROLE_ADMIN"}/>}>
                    <Route path="admin/*" element={
                        <Admin dataProvider={dataProvider} basename="/admin" layout={AdminLayout}>
                            <Resource name="users" options={{label: 'Пользователи'}} list={UserList} edit={UserEdit}/>
                            <Resource name="projects" options={{label: 'Проекты'}} list={ProjectList} edit={ProjectEdit} create={ProjectCreate}/>
                            <Resource name="events" options={{label: 'События'}} list={EventList} edit={EventEdit} create={EventCreate}/>
                            <Resource name="project_jury" options={{label: 'Проверяющие и менторы'}} list={ProjectJuryManagement} edit={ProjectJuryManagement}
                                      create={ProjectJuryManagement}/>
                            <Resource name="project_event" options={{label: 'Этапы отчётности'}} list={ProjectEventManagement} edit={ProjectEventManagement}
                                      create={ProjectEventManagement}/>
                        </Admin>
                    }/>

            </Route>
                {/* Доступны для аутентифицированных */}
                <Route element={<OnlyAuthenticatedRoutes />}>
                    <Route path="/update_profile" element={<UpdateProfilePage />} />
                </Route>
            </Routes>
        </Router>
    );
}

function App() {
    return (
        <AuthProvider>
            <AppContent />
        </AuthProvider>
    );
}

export default App;
