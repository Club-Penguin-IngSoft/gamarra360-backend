import React, { useEffect, useRef } from 'react';
import { GOOGLE_CLIENT_ID } from '../constants/constantes';

interface BotonGoogleProps {
  onSuccess: (idToken: string) => void;
  onError?: (err: string) => void;
}

declare global {
  interface Window {
    google?: any;
  }
}

const BotonGoogle = ({ onSuccess, onError }: BotonGoogleProps) => {
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const idScript = 'google-gsi-client';
    let script = document.getElementById(idScript) as HTMLScriptElement;
    
    const initializeGoogleBtn = () => {
      if (window.google && containerRef.current) {
        window.google.accounts.id.initialize({
          client_id: GOOGLE_CLIENT_ID,
          callback: (response: any) => {
            if (response.credential) {
              onSuccess(response.credential);
            } else if (onError) {
              onError('No se recibió la credencial de Google.');
            }
          },
        });

        window.google.accounts.id.renderButton(containerRef.current, {
          theme: 'outline',
          size: 'large',
          width: '380',
          text: 'continue_with',
          shape: 'rectangular',
        });
      }
    };

    if (!script) {
      script = document.createElement('script');
      script.id = idScript;
      script.src = 'https://accounts.google.com/gsi/client';
      script.async = true;
      script.defer = true;
      script.onload = initializeGoogleBtn;
      document.body.appendChild(script);
    } else {
      if (window.google) {
        initializeGoogleBtn();
      } else {
        script.onload = initializeGoogleBtn;
      }
    }
  }, [onSuccess, onError]);

  return (
    <div className="w-full flex justify-center py-1">
      <div ref={containerRef} className="w-full" style={{ minHeight: '44px' }} />
    </div>
  );
};

export default BotonGoogle;
