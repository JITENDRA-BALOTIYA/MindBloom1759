import { auth } from './config';
import {
  signInWithEmailAndPassword,
  signInWithPopup,
  GoogleAuthProvider,
  createUserWithEmailAndPassword,
  updateProfile,
} from 'firebase/auth';

export const firebaseAuthService = {
  async loginWithEmail(email: string, password: string) {
    try {
      const result = await signInWithEmailAndPassword(auth, email, password);
      return result.user;
    } catch (error) {
      throw this.handleError(error);
    }
  },

  async loginWithGoogle() {
    try {
      const provider = new GoogleAuthProvider();
      provider.setCustomParameters({ prompt: 'select_account' });
      const result = await signInWithPopup(auth, provider);
      return result.user;
    } catch (error) {
      throw this.handleError(error);
    }
  },

  async registerWithEmail(email: string, password: string, displayName: string) {
    try {
      const result = await createUserWithEmailAndPassword(auth, email, password);
      await updateProfile(result.user, { displayName });
      return result.user;
    } catch (error) {
      throw this.handleError(error);
    }
  },

  async logout() {
    try {
      await auth.signOut();
    } catch (error) {
      throw this.handleError(error);
    }
  },

  handleError(error: unknown): Error {
    console.error('Firebase Auth Error:', error);
    if (error instanceof Error) {
      if ('code' in error) {
        const code = (error as any).code;
        const fullMessage = (error as any).message || error.message;
        
        console.error('Error Code:', code);
        console.error('Full Error Message:', fullMessage);
        
        const messages: Record<string, string> = {
          'auth/user-not-found': 'User not found. Please check your email.',
          'auth/wrong-password': 'Incorrect password. Please try again.',
          'auth/email-already-in-use': 'Email already in use.',
          'auth/weak-password': 'Password should be at least 6 characters.',
          'auth/invalid-email': 'Invalid email address.',
          'auth/operation-not-allowed': 'Operation not allowed.',
          'auth/too-many-requests': 'Too many login attempts. Please try again later.',
          'auth/api-key-not-valid': `⚠️ API Key Issue - Code: ${code}. Check Firebase Console API key restrictions.`,
        };
        return new Error(messages[code] || error.message);
      }
      return error;
    }
    return new Error('An unknown error occurred');
  },
};
