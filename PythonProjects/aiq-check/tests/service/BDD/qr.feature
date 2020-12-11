"""This is a feature file for BDD"""
Feature: Check QR code is detected from the image

    Scenario Outline: When QR/Bar code needs to be detected from an image
        Given image to detect
        When <image> is sent
        Then <type> are returned


Examples:
        | image  | type|
        | science-abc-barcode.jpg | QR-Code |
        | bar-code.jpg | Barcode|
