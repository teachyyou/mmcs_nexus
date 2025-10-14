import React, {useEffect, useState} from 'react';
import { useAuth } from '../../AuthContext';

const HomePage = () => {
    const [isLoading, setIsLoading] = useState(true);
    const {isAuthenticated, setUser} = useAuth();

    useEffect(() => {
        if (isAuthenticated) {
            fetch('/api/v1/auth/user', { credentials: 'include' })
                .then(response => response.json())
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
                });
        } else {
            setIsLoading(false);
        }
    }, [isAuthenticated, setUser]);

    if (isLoading) return null;

    return (
        <div className="App">
            {/* контент домашней без шапки — шапка рендерится глобально в AppContent */}
        </div>
    );
};

export default HomePage;
