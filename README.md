# Interface to currencylayer API (free plan)
## Android app that converts between currencies
#### 1. Outline
Currency converter using [currencylayer's free plan](https://currencylayer.com/signup/free). 
An amount of the chosen source currency is automatically converted to all available currencies based on the current daily rate.
Floating point error is not handled.
#### 2. Setup
To run the App, sign up to free plan and replace "FREE_KEY_GOES_HERE" with your own API Access Key before building. 
#### 3. Functionality
##### a) Control Section
- Input Field: Takes the amount of the source currency
- Handle of current source is displayed next to it
- Currency Spinner: Opens when pressing ▼ for selection of source currency
- Text area: Currently selected source in full text, timestamp of current daily rate
##### b) List Area
Displays list of all available target currency and their converted amounproperly amounts. 
Floating point error is not properly handled.
![alt text](https://github.com/ProfessorRino/Playground/blob/master/challengeScreen1.png "screenshot1")
![alt text](https://github.com/ProfessorRino/Playground/blob/master/challengeSourceSelect.png "screenshot2")
