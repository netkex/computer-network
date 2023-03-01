package server

import (
	"context"
	"encoding/json"
	"github.com/go-chi/chi/v5"
	"github.com/go-chi/chi/v5/middleware"
	"io"
	"net/http"
	"strconv"
)

type Server struct {
	port int
	r    *chi.Mux
	db   *ProductDB
}

func NewServer(port int) *Server {
	r := chi.NewRouter()
	r.Use(middleware.Logger)

	s := Server{
		port,
		r,
		newProductDB()}
	s.init()

	return &s
}

func (s *Server) getProductList(w http.ResponseWriter, r *http.Request) {
	productList := s.db.getAll()

	w.Header().Set("content-type", "application/json")
	json.NewEncoder(w).Encode(productList)
}

func (s *Server) addProduct(w http.ResponseWriter, r *http.Request) {
	var productInfo ProductInfo

	if json.NewDecoder(r.Body).Decode(&productInfo) != nil {
		http.Error(w, http.StatusText(500), 500)
		return
	}
	id := s.db.addNewProduct(productInfo)

	w.Header().Set("content-type", "application/json")
	w.Write([]byte(strconv.Itoa(id)))
}

func (s *Server) getProductByID(w http.ResponseWriter, r *http.Request) {
	id, ok := r.Context().Value("id").(int)
	if !ok {
		http.Error(w, http.StatusText(422), 422)
		return
	}

	productInfo := s.db.getProductInfo(id)

	w.Header().Set("content-type", "application/json")
	json.NewEncoder(w).Encode(productInfo)
}

func (s *Server) updateProductByID(w http.ResponseWriter, r *http.Request) {
	id, ok := r.Context().Value("id").(int)
	if !ok {
		http.Error(w, http.StatusText(422), 422)
		return
	}

	var productInfo ProductInfo
	if json.NewDecoder(r.Body).Decode(&productInfo) != nil {
		http.Error(w, http.StatusText(500), 500)
		return
	}
	s.db.updateProduct(id, productInfo)

	w.WriteHeader(200)
}

func (s *Server) deleteProductByID(w http.ResponseWriter, r *http.Request) {
	id, ok := r.Context().Value("id").(int)
	if !ok {
		http.Error(w, http.StatusText(422), 422)
		return
	}

	s.db.deleteProduct(id)

	w.WriteHeader(200)
}

func (s *Server) addImage(w http.ResponseWriter, r *http.Request) {
	id, ok := r.Context().Value("id").(int)
	if !ok {
		http.Error(w, http.StatusText(422), 422)
		return
	}

	image, err := io.ReadAll(r.Body)
	if err != nil {
		http.Error(w, http.StatusText(500), 500)
		return
	}
	s.db.addImage(id, image)

	w.WriteHeader(200)
}

func (s *Server) getImage(w http.ResponseWriter, r *http.Request) {
	id, ok := r.Context().Value("id").(int)
	if !ok {
		http.Error(w, http.StatusText(422), 422)
		return
	}

	image := s.db.getImage(id)

	w.Header().Set("content-type", "image/jpeg")
	w.Write(image)
}

func (s *Server) withProductID(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		id, err := strconv.Atoi(chi.URLParam(r, "id"))
		if err != nil {
			http.Error(w, http.StatusText(404), 404)
		}
		ctx := context.WithValue(r.Context(), "id", id)
		next.ServeHTTP(w, r.WithContext(ctx))
	})
}

func (s *Server) init() {
	s.r.Get("/", s.getProductList)
	s.r.Post("/", s.addProduct)

	s.r.Route("/{id}", func(r chi.Router) {
		r.Use(s.withProductID)

		r.Get("/", s.getProductByID)
		r.Get("/image", s.getImage)

		r.Put("/", s.updateProductByID)
		r.Put("/image", s.addImage)

		r.Delete("/", s.deleteProductByID)
	})
}

func (s *Server) Start() {
	http.ListenAndServe(":"+strconv.Itoa(s.port), s.r)
}
