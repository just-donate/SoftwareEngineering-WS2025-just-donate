import { useRouter } from 'next/navigation';
import { useEffect, ComponentType, useState } from 'react';
import axiosInstance from './axiosInstance';

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
          // Axios automatically throws an error for non-2xx responses.
          await axiosInstance.get('/check-auth');

          // If the request is successful, mark the user as authenticated.
          setIsAuthenticated(true);
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
      return <WrappedComponent {...props} />;
    }
  };

  return RequiresAuth;
};

export default withAuth;
