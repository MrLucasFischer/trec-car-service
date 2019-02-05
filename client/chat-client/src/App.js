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
    fetch(`http://localhost:5901/search?algo=bm25&k1=0.5&b=0.45&q=${this.state.value}`)
      .then(result => result.text())
      .then(data => this.setState({ serverResponse: data }))
    event.preventDefault()
  }

  handleChange(event) {
    console.log("stuff")
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
