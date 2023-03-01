package server

type ProductInfo struct {
	ID    int    `json:"id"`
	Name  string `json:"name"`
	Price int    `json:"price"`
}

type ProductFiles struct {
	Image []byte `json:"image"`
}

type Product struct {
	ProductInfo  `json:"info"`
	ProductFiles `json:"files,omitempty"`
}

type ProductDB struct {
	lastID   int
	products map[int]*Product
}

func newProductDB() *ProductDB {
	return &ProductDB{
		0,
		make(map[int]*Product),
	}
}

func (db *ProductDB) getProductInfo(id int) ProductInfo {
	return db.products[id].ProductInfo
}

func (db *ProductDB) addNewProduct(productInfo ProductInfo) int {
	curID := db.lastID
	db.lastID += 1
	productInfo.ID = curID
	db.products[curID] = &Product{ProductInfo: productInfo}
	return curID
}

func (db *ProductDB) deleteProduct(id int) {
	delete(db.products, id)
}

func (db *ProductDB) addImage(id int, image []byte) {
	db.products[id].ProductFiles.Image = image
}

func (db *ProductDB) getImage(id int) []byte {
	return db.products[id].ProductFiles.Image
}

func (db *ProductDB) updateProduct(id int, productInfo ProductInfo) {
	productInfo.ID = db.products[id].ProductInfo.ID
	db.products[id].ProductInfo = productInfo
}

func (db *ProductDB) getAll() []ProductInfo {
	var productList []ProductInfo
	for _, product := range db.products {
		productList = append(productList, product.ProductInfo)
	}
	return productList
}
