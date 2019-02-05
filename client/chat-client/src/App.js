import React, { Component } from 'react';
import './App.css';

class App extends Component {
  constructor() {
    super();
    this.state = {
      value: '',
      serverResponse: ''
    }
    this.handleSubmit = this.handleSubmit.bind(this);
  }

  handleSubmit(event) {
    var xmlhttp = new XMLHttpRequest();
    xmlhttp.onreadystatechange = function () {
      if (this.readyState == 4 && this.status == 200) {
        console.log(this.responseText)
      }
    };
    xmlhttp.open('GET', "http://localhost:5901/");
    xmlhttp.send();
    event.preventDefault()
    // fetch("http://localhost:5901/").then(result => {
    //   console.log("sutff");
    //   return result.body;
    // }).then(data => console.log(data))
    // event.preventDefault();
  }

  handleChange(event) {
    console.log("stuff")
    this.setState({ value: event.target.value });
  }

  render() {
    return (
      <div className="App">
        <h1>Welcome to the chatbot app!</h1>
        <form onSubmit={this.handleSubmit}>


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
