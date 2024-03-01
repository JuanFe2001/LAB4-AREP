const client = (() => {
    const API_URL = "http://www.omdbapi.com/?apikey=f70773ff&t=";
    const RESPONSE_CONTAINER_ID = "getrespmsg";

    function loadGetMsg() {
        const nameVar = document.getElementById("name").value;
        const xhttp = new XMLHttpRequest();
        const encodedName = encodeURIComponent(nameVar);

        xhttp.onload = function () {
            if (this.status === 200) {
                const contentType = this.getResponseHeader("Content-Type");
                if (contentType && contentType.includes("application/json")) {
                    const movieData = JSON.parse(this.responseText);
                    displayMovieDetails(movieData);
                } else {
                    console.error("Invalid response format");
                }
            } else {
                console.error("Request failed with status:", this.status);
            }
        };

        xhttp.onerror = function () {
            console.error("Request failed");
        };

        xhttp.open("GET", API_URL + encodedName);
        xhttp.send();
    }

    function displayMovieDetails(movieData) {
        const poster = document.getElementById("poster");
        const title = document.querySelector("#getrespmsg .title");
        const director = document.querySelector("#getrespmsg .director");
        const released = document.querySelector("#getrespmsg .released");
        const actors = document.querySelector("#getrespmsg .actors");
        const language = document.querySelector("#getrespmsg .language");
        const plot = document.querySelector("#getrespmsg .plot");

        poster.src = movieData.Poster;
        title.textContent = "Título: " + movieData.Title;
        director.textContent = "Director: " + movieData.Director;
        released.textContent = "Fecha de lanzamiento: " + movieData.Released;
        actors.textContent = "Actores: " + movieData.Actors;
        language.textContent = "Idioma: " + movieData.Language;
        plot.textContent = "Trama: " + movieData.Plot;
    }

    return {
        loadGetMsg
    };
})();
