<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>Chatbot</title>
</head>
<body>
    <h1>Welcome to the Chatbot!</h1>
    <form action="/search_web">

        Insert Similarity to use:
        <br/>
        <input type="radio" name="sim" value="bm25" checked> BM25<br>
        <input type="radio" name="sim" value="lmd"> Language Model w/ Dirichlet Smoothing<br>
        <br/>

        Insert your Similarities parameters:
        <br/>


        Insert your query:
        <br/>
        <input type="text" name="query_text_field" value="">
        <br/>

        <input type="submit" value="Submit">
    </form>
</body>
</html>