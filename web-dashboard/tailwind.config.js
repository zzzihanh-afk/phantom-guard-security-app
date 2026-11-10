/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        phantom: {
          bg: '#070B19',
          surface: '#0F1A36',
          elevated: '#15264F',
          border: '#1D3265',
          neon: '#1D72FE',
          cyan: '#00D2FF',
          accent: '#38BDF8',
          green: '#05D182',
          amber: '#F59E0B',
          red: '#EF4444',
          text: '#FFFFFF',
          secondary: '#94A3B8',
          muted: '#64748B'
        }
      }
    },
  },
  plugins: [],
}
