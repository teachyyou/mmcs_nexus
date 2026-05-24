import { useAuth } from './AuthContext';
import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import ProtectedAuthenticationRoutes from './components/routes/ProtectedAuthenticationRoutes';
import HomePage from './components/home/HomePage';
import LoginPage from './components/login/LoginPage';
import AuthenticatedAndVerifiedRoutes from './components/routes/AuthenticatedAndVerifiedRoutes';
import GradeTablePage from './components/jury/GradeTablePage';
import { Admin, Resource } from 'react-admin';
import AdminLayout from './components/admin/AdminLayout';
import UserList from './components/admin/userlist/UserList';
import UserEdit from './components/admin/userlist/UserEdit';
import ProjectList from './components/admin/project/ProjectList';
import { ProjectCreate, ProjectEdit } from './components/admin/project/ProjectCE';
import EventList from './components/admin/event/EventList';
import { EventCreate, EventEdit } from './components/admin/event/EventCE';
import ProjectJuryManagement from './components/admin/ProjectJuryManagement';
import ProjectEventManagement from './components/admin/ProjectEventManagement';
import ProjectEventDayManagement from './components/admin/ProjectEventDayManagement';
import OnlyAuthenticatedRoutes from './components/routes/OnlyAuthenticatedRoutes';
import UpdateProfilePage from './components/login/UpdateProfilePage';
import springBootRestProvider from './components/admin/restProviders/springBootRestProvider';
import LoadingCard from './components/common/LoadingCard';
import { useTheme } from '@mui/material/styles';
import { Box } from '@mui/material';
import NavigationBar from './components/home/NavigationBar';
import i18nProvider from './components/admin/i18nProvider';
import { PostCreate, PostEdit } from './components/admin/post/PostCE';
import PostList from './components/admin/post/PostList';
import PostPage from './components/home/PostPage';
import ProjectsPage from './components/projects/ProjectsPage';
import ProjectPage from './components/projects/ProjectPage';

const dataProvider = springBootRestProvider('/api/v1/admin');

export default function AppContent() {
    const outerTheme = useTheme();
    const {
        setIsAuthenticated,
        setUserStatus,
        setUserRole,
        setUserId,
        setUser,
        setCaptainProject,
    } = useAuth();
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        fetch('/api/v1/auth/status', { credentials: 'include' })
            .then(response => response.json())
            .then(data => {
                setIsAuthenticated(data.isAuthenticated);
                setUserRole(data.userRole);
                setUserStatus(data.userStatus);
                setCaptainProject(data.captainProject || null);

                if (data.user) {
                    setUserId(data.userId);
                    setUser(prev => ({ ...prev, ...data.user }));
                } else {
                    setUserId(null);
                    setUser(null);
                }

                setIsLoading(false);
            })
            .catch(() => {
                setIsAuthenticated(false);
                setUserRole(null);
                setUserStatus(null);
                setUserId(null);
                setUser(null);
                setCaptainProject(null);
                setIsLoading(false);
            });
    }, [
        setIsAuthenticated,
        setUserStatus,
        setUserRole,
        setUserId,
        setUser,
        setCaptainProject,
    ]);

    if (isLoading) return <LoadingCard />;

    return (
        <Router>
            <NavigationBar />
            <Box>
                <Routes>
                    <Route element={<ProtectedAuthenticationRoutes />}>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/posts/:id" element={<PostPage />} />
                        <Route path="/login" element={<LoginPage />} />
                    </Route>

                    <Route element={<AuthenticatedAndVerifiedRoutes roleRequired="ROLE_USER" />}>
                        <Route path="/projects" element={<ProjectsPage />} />
                        <Route path="/projects/:id" element={<ProjectPage />} />
                    </Route>

                    <Route element={<AuthenticatedAndVerifiedRoutes roleRequired="ROLE_JURY" />}>
                        <Route path="/grades" element={<GradeTablePage />} />
                    </Route>

                    <Route element={<AuthenticatedAndVerifiedRoutes roleRequired="ROLE_ADMIN" />}>
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
                                    <Resource name="users" options={{ label: 'Пользователи' }} list={UserList} edit={UserEdit} />
                                    <Resource name="projects" options={{ label: 'Проекты' }} list={ProjectList} edit={ProjectEdit} create={ProjectCreate} />
                                    <Resource name="events" options={{ label: 'Этапы отчётности' }} list={EventList} edit={EventEdit} create={EventCreate} />
                                    <Resource name="project_event" options={{ label: 'Назначение этапов' }} list={ProjectEventManagement} edit={ProjectEventManagement} create={ProjectEventManagement} />
                                    <Resource name="project_event_days" options={{ label: 'Дни защиты' }} list={ProjectEventDayManagement} edit={ProjectEventDayManagement} create={ProjectEventDayManagement} />
                                    <Resource name="project_jury" options={{ label: 'Назначение жюри' }} list={ProjectJuryManagement} edit={ProjectJuryManagement} create={ProjectJuryManagement} />
                                    <Resource
                                        name="posts"
                                        options={{ label: 'Посты' }}
                                        list={PostList}
                                        edit={PostEdit}
                                        create={PostCreate}
                                    />
                                </Admin>
                            }
                        />
                    </Route>

                    <Route element={<OnlyAuthenticatedRoutes />}>
                        <Route path="/update_profile" element={<UpdateProfilePage />} />
                    </Route>
                </Routes>
            </Box>
        </Router>
    );
}