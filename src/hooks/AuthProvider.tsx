import { createContext, ReactNode, useEffect, useState, useContext } from 'react';
import { User as FirebaseUser } from 'firebase/auth';
import { auth } from '../firebase/config';
import { User } from '../types/index';

interface AuthContextType {
  user: User | null;
  loading: boolean;
  error: Error | null;
  logout: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<Error | null>(null);

  useEffect(() => {
    const unsubscribe = auth.onAuthStateChanged(
      (firebaseUser: FirebaseUser | null) => {
        try {
          if (firebaseUser) {
            setUser({
              uid: firebaseUser.uid,
              email: firebaseUser.email || '',
              displayName: firebaseUser.displayName || 'Admin',
              photoURL: firebaseUser.photoURL || undefined,
              role: 'admin',
            });
          } else {
            setUser(null);
          }
        } catch (err: any) {
          setError(err instanceof Error ? err : new Error('Failed to load user'));
        } finally {
          setLoading(false);
        }
      },
      (err: any) => {
        setError(err instanceof Error ? err : new Error('Auth error'));
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, []);

  const logout = async () => {
    try {
      await auth.signOut();
      setUser(null);
    } catch (err: any) {
      setError(err instanceof Error ? err : new Error('Failed to logout'));
    }
  };

  const value: AuthContextType = { user, loading, error, logout };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
