"""This is a feature file for BDD"""
Feature: Check QR code is detected from the image

    Scenario Outline: When QR/Bar code needs to be detected from an image
        Given <image> to detect
        When <image> is sent
        Then <type> and <content> are returned


Examples:
        | image  | type| content |
        | science-abc-barcode.jpg | QR-Code | http://www.scienceabc.com |
        | bar-code.jpg | Barcode| 051111407592 |
