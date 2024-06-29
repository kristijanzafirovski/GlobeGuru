document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('form-id'); // Ensure the form has the correct ID

    form.addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent the default form submission

        // Collect form data
        const formData = new FormData(form);
        const data = {};
        formData.forEach((value, key) => {
            data[key] = value;
        });

        // Send form data to the server
        fetch('/submit', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
            .then(response => response.json())
            .then(data => {
                // Process the scraped data and update the page
                const dataList = document.getElementById('givenOptions');
                dataList.innerHTML = ''; // Clear previous data

                data.forEach(item => {
                    // Create option container div
                    const optionDiv = document.createElement('div');
                    optionDiv.classList.add('option');

                    // Create and append image element
                    const img = document.createElement('img');
                    img.classList.add('image');
                    img.src = item.imgSrc; // Use item.imageSrc if available
                    optionDiv.appendChild(img);

                    // Create and append hotel name paragraph
                    const nameParagraph = document.createElement('p');
                    nameParagraph.id = 'name';
                    nameParagraph.textContent = item.hotelName;
                    optionDiv.appendChild(nameParagraph);

                    // Create and append country paragraph
                    const countryParagraph = document.createElement('p');
                    countryParagraph.id = 'country';
                    countryParagraph.style.fontSize = '20px';
                    countryParagraph.textContent = item.country;
                    optionDiv.appendChild(countryParagraph);

                    // Create and append price heading and paragraph
                    const priceHeading = document.createElement('h1');
                    priceHeading.textContent = 'Цена:';
                    optionDiv.appendChild(priceHeading);
                    const priceParagraph = document.createElement('h2');
                    priceParagraph.id = 'price';
                    priceParagraph.textContent = item.price;
                    optionDiv.appendChild(priceParagraph);

                    // Create link and button
                    const link = document.createElement('a');
                    link.id = 'link';
                    link.href = item.link; // Use item.link if available
                    link.target = '_blank'; // Open link in new tab

                    const button = document.createElement('button');
                    button.classList.add('btn');
                    button.textContent = 'Линк до страна';

                    link.appendChild(button);
                    optionDiv.appendChild(link);

                    // Append option div to dataList
                    dataList.appendChild(optionDiv);
                });
            })
            .catch(error => {
                console.error('Error fetching data:', error);
            });
    });
});
