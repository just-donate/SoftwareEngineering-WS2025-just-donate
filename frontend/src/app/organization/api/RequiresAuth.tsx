import { useRouter } from 'next/navigation';
import { useEffect, ComponentType, useState } from 'react';
import axiosInstance from '../api/axiosInstance';

const withAuth = <P extends object>(
  WrappedComponent: React.ComponentType<P>,
): React.FC<P> => {
  const RequiresAuth: React.FC<P> = (props) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [loading, setLoading] = useState(true);
    const router = useRouter();

    useEffect(() => {
      const checkAuth = async () => {
        try {
          const response = await fetch('/api/check-auth');
          if (response.ok) {
            setIsAuthenticated(true);
          } else {
            router.push('/organization/login');
          }
        } catch (error) {
          console.error('Error during authentication check:', error);
          router.push('/organization/login');
        } finally {
          setLoading(false);
        }
      };

      checkAuth();
    }, [router]);

    if (loading) {
      return <div>Loading...</div>; // Render a loading indicator while checking
    }

    if (isAuthenticated) {
      return <WrappedComponent {...props}/>
    }
  };

  return RequiresAuth;
};

export default withAuth;
