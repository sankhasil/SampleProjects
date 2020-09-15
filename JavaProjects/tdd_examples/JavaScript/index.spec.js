const {sum,calculator_add} = require('./index');


describe('should add two numbers or concatenate strings', () => {
    test('adds 1 + 2 to equal 3', () => {
        expect(sum(1, 2)).toBe(3);
    });

    test('concatenate "We love " + "you JS" to equal "We love you JS"', () => {
        expect(sum('We love ', 'you JS')).toBe('We love you JS');
    });
});


describe('should add two numbers if provided as comma separated or return 0 if empty string or return the single content for without comma separated content', () => {
    test('adds 1 + 2 to equal 3', () => {
        expect(calculator_add('1, 2')).toBe(3);
    });

    test('returns the single content', () => {
        expect(calculator_add('1')).toBe(1);
    });

    test('returns 0 for "" content', () => {
        expect(calculator_add()).toBe(0);
    });
});

