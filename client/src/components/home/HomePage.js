import React, {useEffect, useState} from 'react';
import NavigationBar from './NavigationBar';
import {StyledEngineProvider} from "@mui/material";
import { useAuth } from '../../AuthContext';


const HomePage = () => {

    const [isLoading, setIsLoading] = useState(true);
    const [name, setName] = useState("Unknown User");
    const [avatarUrl, setAvatarUrl] = useState("default_url");
    const {isAuthenticated,setUser, user} = useAuth();

    useEffect(() => {
        if (isAuthenticated) {
            fetch('http://localhost:8080/api/v1/auth/user', {
                credentials: 'include'
            })
                .then(
                    response => response.json())
                .then(data => {
                    if (data.user) {
                        setUser({
                            login: data.user.login,
                            github_name: data.user.github_name,
                            firstname: data.user.firstname,
                            lastname: data.user.lastname,
                            avatar_url: data.user.avatar_url,
                            email: data.user.email,
                            course: data.user.course,
                            group: data.user.group,
                        });
                    }
                    setIsLoading(false);
                })
        }
        else {
            setName("Guest");
            setAvatarUrl(null)
            setIsLoading(false)
        }
    }, [isAuthenticated]);

    if (!isLoading) return (
        <div className="App">
            <header className="App-header">
                <StyledEngineProvider injectFirst>
                    <NavigationBar className='navigation-bar' avatarUrl={user.avatar_url} userName={name} userEmail={"test@mail.ru"}/>
                </StyledEngineProvider>
            </header>
        </div>
    );
};

export default HomePage;