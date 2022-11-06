import React, { Component } from "react"
import Grid from "@mui/material/Grid"
import { Box } from "@mui/system"
import { Select, MenuItem, FormControl, InputLabel } from "@mui/material"
import Carousel from "react-multi-carousel"

import CardComponent from "../components/Card"
import CarouselComponent from "../components/Carousel"
import ProductService from "../services/ProductService"
import StorageHelper from "../services/StorageHelper"
import "../styles/MainStyle.css"

const responsive = {
    desktop: {
        breakpoint: { max: 3000, min: 1024 },
        items: 3,
        slidesToSlide: 2, // optional, default to 1.
    },
}

class Product extends Component {
    constructor(props) {
        super(props)
        this.state = {
            url: "product/",
            data: [],
            mainData: [],
            recommendData: [],
            searchText: "",
            filter: ""
        }
    }

    async componentDidMount() {
        const firstTime = StorageHelper.getFirstTime()
        if (firstTime === true) {
            this.props.history.push("/recommendation")
        }
        const recommend = await ProductService.getRecommendation(StorageHelper.getUserId())
        const res = await ProductService.getProducts()
        console.log(recommend);
        let recData
        res.data.forEach(element => {
            if(element.category === recommend){
                this.state.recommendData.push(element)
            }
        })
        //this.setState({recommendData: recData})
        this.setState({ mainData: res.data })
        this.setState({ data: res.data })
    }

    render() {
        return (
            <Box className="container">
                <Box style={{paddingTop: '1%'}}>
                <input style={{height: 55, width: '60%'}}
                    type="search"
                    id="search"
                    placeholder="Search for..."
                    value={this.state.searchText}
                    onChange={(e) => {
                        if(this.state.filter !== ""){
                            const dataSet = []
                            this.state.mainData.forEach(element => {    
                                if(element.category.toLowerCase().indexOf(this.state.filter.toLowerCase())  > -1) {
                                    dataSet.push(element)
                                }
                            })

                            const newDataSet = []
                            this.setState({searchText: e.target.value})
                            dataSet.forEach(element => {
                                if(element.productName.toLowerCase().indexOf(e.target.value.toLowerCase())  > -1) {
                                    newDataSet.push(element)
                                }
                            })
                            this.setState({ data: newDataSet })
                        }
                        else {
                            const dataSet = []
                            this.setState({searchText: e.target.value})
                            this.state.mainData.forEach(element => {
                                if(element.productName.toLowerCase().indexOf(e.target.value.toLowerCase())  > -1) {
                                    dataSet.push(element)
                                }
                            })
                            this.setState({ data: dataSet })
                        }
                        
                    }}
                />
                <FormControl style={{ width: 200 , left: '1%'}}>
                    <InputLabel>Category</InputLabel>
                    <Select
                        label="Category"
                        value = {this.state.filter}
                        onChange={(e) => {
                            if (this.state.searchText !== ""){
                                const dataSet = []
                                this.state.mainData.forEach(element => {
                                    if(element.productName.toLowerCase().indexOf(this.state.searchText.toLowerCase())  > -1) {
                                        dataSet.push(element)
                                    }
                                })
                                
                                const newDataSet = []
                                this.setState({ filter: e.target.value})
                                dataSet.forEach(element => {    
                                    if(element.category.toLowerCase().indexOf(e.target.value.toLowerCase())  > -1) {
                                        newDataSet.push(element)
                                    }
                                })
                                this.setState({ data: newDataSet })

                            } else {
                                const dataSet = []
                                this.setState({ filter: e.target.value})
                                
                                this.state.mainData.forEach(element => {    
                                    if(element.category.toLowerCase().indexOf(e.target.value.toLowerCase())  > -1) {
                                        dataSet.push(element)
                                    }
                                })
                                this.setState({ data: dataSet })
                            }
                        }}
                    >
                        <MenuItem value={""}>All</MenuItem>
                        <MenuItem value={"BOOKS"}>Books</MenuItem>
                        <MenuItem value={"ELECTRONICS"}>Electronics</MenuItem>
                        <MenuItem value={"FASHION"}>Fashion</MenuItem>
                        <MenuItem value={"FOOD"}>Food</MenuItem>
                        <MenuItem value={"TOYS"}>Toys</MenuItem>
                        <MenuItem value={"UTILITY"}>Utility</MenuItem>
                        <MenuItem value={"VIDEO GAMES"}>Video Games</MenuItem>
                        <MenuItem value={"OTHERS"}>Others</MenuItem>
                    </Select>
                </FormControl>
                </Box>
                <br></br>
                <Carousel responsive={responsive}>
                    {this.state.recommendData.map((data, i) => (
                        <CarouselComponent
                            title={data.productName}
                            condition={data.condition}
                            address={data.address}
                            imgSource={data.imageUrl}
                            category={data.category}
                            buttonLink={this.state.url + data.id}></CarouselComponent>
                    ))}
                </Carousel>
                <br></br>
                <Box>
                    <Grid container rowSpacing={2} columnSpacing={{ xs: 1, sm: 2, md: 2 }}>
                        {this.state.data.map((data, i) => (
                            <Grid item xs={3}>
                                <CardComponent
                                    title={data.productName}
                                    description={data.description}
                                    address={data.address}
                                    condition={data.condition}
                                    imgSource={data.imageUrl}
                                    buttonLink={this.state.url + data.id}
                                    dateTime={data.dateTime}
                                    ownerName={data.ownerName}></CardComponent>
                            </Grid>
                        ))}
                    </Grid>
                </Box>
            </Box>
        )
    }
}

export default Product
