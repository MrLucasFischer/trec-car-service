import React, { Component } from 'react';
import './App.css';

class App extends Component {
  constructor() {
    super();
    this.state = {
      value: '',
      serverResponse: ''
    }
  }

  handleSubmit(event) {
    fetch("localhost:5901")
      .then(results => {
        console.log(results)
        return results.body;
      })
  }

  handleChange(event) {
    this.setState({ value: event.target.value });
  }

  render() {
    return (
      <div className="App">
        <h1>Welcome to the chatbot app!</h1>
        <form onSubmit={(event) => this.handleSubmit(event)}>


          <label>
            Insert your query:
            <br />
            <input type="text" value={this.state.value} onChange={(event) => this.handleChange(event)} />
          </label>

          <br />
          <input type="submit" value="Submit" />
        </form>
      </div>
    );
  }
}

export default App;
