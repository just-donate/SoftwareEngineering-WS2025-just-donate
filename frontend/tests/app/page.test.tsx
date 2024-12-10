// MyPage.test.js
import React from 'react';
import { render } from '@testing-library/react';
import Home from "@/app/page"; // Import your component

describe('MyPage', () => {
    it('renders correctly', async () => {
        const { getByText } = render(<Home />);
        expect(getByText('My Page Title')).not.toBeNull();
    });
});