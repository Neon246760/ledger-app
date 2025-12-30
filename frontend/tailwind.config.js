/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#8B5CF6',
        secondary: '#F3F4F6', // Gray-100
        accent: '#F97316', // Orange-500
        silver: '#6B7280', // Gray-500
        background: {
          DEFAULT: '#FFFFFF',
          primary: '#FFFFFF',
          secondary: '#F9FAFB', // Gray-50
          card: '#FFFFFF',
        },
        text: {
          primary: '#1F2937', // Gray-800
          secondary: '#4B5563', // Gray-600
          accent: '#F97316', // Orange-500
        }
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      borderRadius: {
        DEFAULT: '0.5rem', // Rounded corners
        'lg': '0.75rem',
        'xl': '1rem',
      }
    },
  },
  plugins: [],
}
