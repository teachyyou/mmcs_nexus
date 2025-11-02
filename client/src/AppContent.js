import {useAuth} from "./AuthContext";
import React, {useEffect, useState} from "react";
import {BrowserRouter as Router, Route, Routes} from "react-router-dom";
import ProtectedAuthenticationRoutes from "./components/routes/ProtectedAuthenticationRoutes";
import HomePage from "./components/home/HomePage";
import LoginPage from "./components/login/LoginPage";
import AuthenticatedAndVerifiedRoutes from "./components/routes/AuthenticatedAndVerifiedRoutes";
import GradeTablePage from "./components/jury/GradeTablePage";
import {Admin, Resource} from "react-admin";
import AdminLayout from "./components/admin/AdminLayout";
import UserList from "./components/admin/userlist/UserList";
import UserEdit from "./components/admin/userlist/UserEdit";
import ProjectList from "./components/admin/project/ProjectList";
import {ProjectCreate, ProjectEdit} from "./components/admin/project/ProjectCE";
import EventList from "./components/admin/event/EventList";
import {EventCreate, EventEdit} from "./components/admin/event/EventCE";
import ProjectJuryManagement from "./components/admin/ProjectJuryManagement";
import ProjectEventManagement from "./components/admin/ProjectEventManagement";
import ProjectEventDayManagement from "./components/admin/ProjectEventDayManagement";
import OnlyAuthenticatedRoutes from "./components/routes/OnlyAuthenticatedRoutes";
import UpdateProfilePage from "./components/login/UpdateProfilePage";
import springBootRestProvider from "./components/admin/restProviders/springBootRestProvider";
import LoadingCard from "./components/common/LoadingCard";
import {useTheme} from "@mui/material/styles";
import { Box } from "@mui/material";
import NavigationBar from "./components/home/NavigationBar";
import i18nProvider from './components/admin/i18nProvider';

const dataProvider = springBootRestProvider('/api/v1/admin');
const APPBAR_H = 64;

export default function AppContent() {
    const outerTheme = useTheme();
    const { user, setIsAuthenticated, setUserStatus, setUserRole, setUserId, setUser } = useAuth();
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetch('/api/v1/auth/status', { credentials: 'include' })
            .then(response => response.json())
            .then(data => {
                setIsAuthenticated(data.isAuthenticated);
                setUserRole(data.userRole);
                setUserStatus(data.userStatus);
                if (data.user) {
                    setUserId(data.userId);
                    setUser(prev => ({ ...prev, ...data.user }));
                }
                setIsLoading(false);
            });
    }, [setIsAuthenticated, setUserStatus, setUserRole, setUserId, setUser]);

    if (isLoading) return <LoadingCard />;

    return (
        <Router>
            {/* фиксированная шапка поверх ВСЕГО */}
            <NavigationBar />
            {/* единый отступ под шапку для всех страниц */}
            <Box>
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

                    {/* Доступны для аутентифицированных и верифицированных (ТОЛЬКО АДМИНЫ) */}
                    <Route element={<AuthenticatedAndVerifiedRoutes roleRequired={"ROLE_ADMIN"}/>}>
                        <Route
                            path="admin/*"
                            element={
                                <Admin
                                    dataProvider={dataProvider}
                                    basename="/admin"
                                    layout={AdminLayout}
                                    theme={outerTheme}
                                    darkTheme={outerTheme}
                                    i18nProvider={i18nProvider}
                                >
                                    <Resource name="users" options={{label: 'Пользователи'}} list={UserList} edit={UserEdit}/>
                                    <Resource name="projects" options={{label: 'Проекты'}} list={ProjectList} edit={ProjectEdit} create={ProjectCreate}/>
                                    <Resource name="events" options={{label: 'События'}} list={EventList} edit={EventEdit} create={EventCreate}/>
                                    <Resource name="project_jury" options={{label: 'Проверяющие и менторы'}} list={ProjectJuryManagement} edit={ProjectJuryManagement} create={ProjectJuryManagement}/>
                                    <Resource name="project_event" options={{label: 'Этапы отчётности'}} list={ProjectEventManagement} edit={ProjectEventManagement} create={ProjectEventManagement}/>
                                    <Resource name="project_event_days" options={{label: 'Дни защиты'}} list={ProjectEventDayManagement} edit={ProjectEventDayManagement} create={ProjectEventDayManagement}/>
                                </Admin>
                            }
                        />
                    </Route>

                    {/* Доступны для аутентифицированных */}
                    <Route element={<OnlyAuthenticatedRoutes />}>
                        <Route path="/update_profile" element={<UpdateProfilePage />} />
                    </Route>
                </Routes>
            </Box>
        </Router>
    );
}
